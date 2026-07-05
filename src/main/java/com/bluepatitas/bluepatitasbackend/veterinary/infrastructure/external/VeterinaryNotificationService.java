package com.bluepatitas.bluepatitasbackend.veterinary.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import com.bluepatitas.bluepatitasbackend.shared.infrastructure.external.FcmNotificationService;

/**
 * VeterinaryNotificationService
 * <p>
 * Infrastructure adapter (Anti-Corruption Layer) that integrates the Veterinary
 * Bounded Context with an external notification delivery system. Responsible for
 * sending real-time alerts to veterinarians when critical animal health events
 * occur, such as perimeter breaches, critical health status changes, or
 * environmental threshold violations.
 * </p>
 *
 * <p><b>Responsibility:</b> This service is the single integration point with
 * the notification platform (e.g., push notification gateway, email service, or
 * SMS provider). Any change to the external notification API contract is
 * contained here.</p>
 *
 * <p><b>Configuration:</b> Set the notification service URL in
 * {@code application.properties}:
 * <pre>
 *   bluepatitas.notifications.url=http://notification-service:9000
 *   bluepatitas.notifications.api-key=your-notification-api-key
 * </pre>
 * </p>
 */
@Slf4j
@Service
public class VeterinaryNotificationService {

    private final RestTemplate restTemplate;
    private final String notificationServiceUrl;
    private final String apiKey;
    private final FcmNotificationService fcmNotificationService;
    private final com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.DeviceTokenRepository deviceTokenRepository;

    public VeterinaryNotificationService(
            RestTemplate restTemplate,
            @Value("${bluepatitas.notifications.url:http://localhost:9000}") String notificationServiceUrl,
            @Value("${bluepatitas.notifications.api-key:dev-notify-key}") String apiKey,
            FcmNotificationService fcmNotificationService,
            com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.DeviceTokenRepository deviceTokenRepository) {
        this.restTemplate = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
        this.apiKey = apiKey;
        this.fcmNotificationService = fcmNotificationService;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public boolean notifyVeterinarian(Long veterinarianId, UUID animalId,
                                      String alertType, String message) {
        log.info("Preparing FCM push notification for veterinarianId={} animalId={}", veterinarianId, animalId);
        
        java.util.List<com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.DeviceToken> tokens = deviceTokenRepository.findAllByUserId(veterinarianId);
        for (var token : tokens) {
            fcmNotificationService.sendPushNotification(token.getToken(), alertType, message);
        }
        
        // We keep the old REST call for backward compatibility with the external service
        String endpoint = notificationServiceUrl + "/api/notifications/veterinarian";
        log.info("Sending {} alert notification to veterinarianId={} for animalId={}",
                alertType, veterinarianId, animalId);

        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> payload = Map.of(
                    "veterinarianId", veterinarianId.toString(),
                    "animalId",       animalId.toString(),
                    "alertType",      alertType,
                    "message",        message
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, request, Void.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            log.error("Failed to deliver alert notification to veterinarianId={}: {}",
                    veterinarianId, ex.getMessage());
            return false;
        }
    }

    public boolean broadcastToAllVeterinarians(String alertType, String message) {
        // FCM Broadcast to a topic
        log.info("Broadcasting via FCM to /topics/veterinarians");
        fcmNotificationService.sendPushNotification("/topics/veterinarians", alertType, message);

        String endpoint = notificationServiceUrl + "/api/notifications/broadcast";
        log.info("Broadcasting {} alert to all veterinarians", alertType);

        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> payload = Map.of(
                    "alertType", alertType,
                    "message",   message
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, request, Void.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            log.error("Failed to broadcast {} alert: {}", alertType, ex.getMessage());
            return false;
        }
    }

    public boolean checkNotificationServiceHealth() {
        return true;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        return headers;
    }
}

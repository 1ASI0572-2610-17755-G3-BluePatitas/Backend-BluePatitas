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

    /**
     * Constructs the adapter with required configuration.
     *
     * @param restTemplate          the shared HTTP client bean
     * @param notificationServiceUrl the base URL of the notification service
     * @param apiKey                 the API key for authentication
     */
    public VeterinaryNotificationService(
            RestTemplate restTemplate,
            @Value("${bluepatitas.notifications.url:http://localhost:9000}") String notificationServiceUrl,
            @Value("${bluepatitas.notifications.api-key:dev-notify-key}") String apiKey) {
        this.restTemplate          = restTemplate;
        this.notificationServiceUrl = notificationServiceUrl;
        this.apiKey                = apiKey;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Notification Commands
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sends an alert notification to a specific veterinarian, informing them
     * of a monitoring event that requires their review.
     * <p>
     * Typical use case: a perimeter breach alert has been created for an animal
     * assigned to the given veterinarian, and they need to be notified immediately.
     * </p>
     *
     * @param veterinarianId the UUID of the veterinarian to notify
     * @param animalId       the UUID of the animal involved in the alert
     * @param alertType      the type of alert (e.g., "PERIMETER_BREACH", "CRITICAL_HEALTH")
     * @param message        the human-readable notification message body
     * @return true if the notification was delivered successfully, false otherwise
     */
    public boolean notifyVeterinarian(UUID veterinarianId, UUID animalId,
                                      String alertType, String message) {
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

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Alert notification DELIVERED to veterinarianId={}", veterinarianId);
            } else {
                log.warn("Notification service returned non-2xx status={} for veterinarianId={}",
                        response.getStatusCode(), veterinarianId);
            }
            return success;

        } catch (RestClientException ex) {
            log.error("Failed to deliver alert notification to veterinarianId={}: {}",
                    veterinarianId, ex.getMessage());
            return false;
        }
    }

    /**
     * Sends a broadcast notification to all veterinarians registered in the system
     * for urgent platform-wide alerts (e.g., system maintenance, global health warnings).
     *
     * @param alertType the type of the broadcast alert
     * @param message   the notification message body
     * @return true if the broadcast was accepted by the notification service
     */
    public boolean broadcastToAllVeterinarians(String alertType, String message) {
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

            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Broadcast {} for alertType={}", success ? "SENT" : "FAILED", alertType);
            return success;

        } catch (RestClientException ex) {
            log.error("Failed to broadcast {} alert: {}", alertType, ex.getMessage());
            return false;
        }
    }

    /**
     * Checks connectivity with the external notification service.
     *
     * @return true if the notification service is reachable and operational
     */
    public boolean checkNotificationServiceHealth() {
        String endpoint = notificationServiceUrl + "/actuator/health";
        log.debug("Checking notification service health at {}", endpoint);

        try {
            ResponseEntity<Void> response = restTemplate.getForEntity(endpoint, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException ex) {
            log.warn("Notification service health check failed: {}", ex.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Api-Key", apiKey);
        return headers;
    }
}

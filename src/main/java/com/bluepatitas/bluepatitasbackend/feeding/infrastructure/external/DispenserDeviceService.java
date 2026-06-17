package com.bluepatitas.bluepatitasbackend.feeding.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * DispenserDeviceService
 * <p>
 * Infrastructure adapter (Anti-Corruption Layer) that integrates the Feeding
 * Bounded Context with the IoT dispenser hardware through the Edge API Gateway.
 * Translates domain feeding commands into HTTP requests to the gateway, decoupling
 * the domain from the specifics of the IoT protocol and device topology.
 * </p>
 *
 * <p><b>Responsibility:</b> This service is the single point of integration with
 * the physical dispenser device. Any change to the Edge API Gateway contract
 * (endpoint path, payload schema, authentication) is contained here.</p>
 *
 * <p><b>Configuration:</b> Set the gateway base URL in {@code application.properties}:
 * <pre>
 *   bluepatitas.edge.gateway.url=http://edge-gateway:8090
 *   bluepatitas.edge.gateway.api-key=your-api-key
 * </pre>
 * </p>
 */
@Slf4j
@Service
public class DispenserDeviceService {

    private final RestTemplate restTemplate;
    private final String gatewayBaseUrl;
    private final String apiKey;

    /**
     * Constructs the adapter with required configuration.
     *
     * @param gatewayBaseUrl the base URL of the Edge API Gateway
     * @param apiKey         the API key for gateway authentication
     */
    public DispenserDeviceService(
            RestTemplate restTemplate,
            @Value("${bluepatitas.edge.gateway.url:http://localhost:8090}") String gatewayBaseUrl,
            @Value("${bluepatitas.edge.gateway.api-key:dev-key}") String apiKey) {
        this.restTemplate   = restTemplate;
        this.gatewayBaseUrl = gatewayBaseUrl;
        this.apiKey         = apiKey;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Dispenser Commands
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sends a dispense command to the IoT Edge API Gateway to trigger the
     * physical food dispenser for a specific feeding plan execution.
     *
     * @param feedingPlanId the ID of the feeding plan triggering the dispense
     * @param animalId      the ID of the animal being fed
     * @param quantity      the amount of food to dispense
     * @param unit          the unit of the food quantity (e.g., "grams")
     * @return true if the gateway acknowledged the command successfully, false otherwise
     */
    public boolean dispenseFood(UUID feedingPlanId, UUID animalId, BigDecimal quantity, String unit) {
        String endpoint = gatewayBaseUrl + "/api/devices/dispenser/dispense";
        log.info("Sending dispense command to Edge Gateway: planId={}, animalId={}, quantity={} {}",
                feedingPlanId, animalId, quantity, unit);

        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> payload = Map.of(
                    "feedingPlanId", feedingPlanId.toString(),
                    "animalId",      animalId.toString(),
                    "quantity",      quantity,
                    "unit",          unit
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, request, Void.class);

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Dispense command ACKNOWLEDGED by Edge Gateway for planId={}", feedingPlanId);
            } else {
                log.warn("Edge Gateway returned non-2xx status={} for planId={}",
                        response.getStatusCode(), feedingPlanId);
            }
            return success;

        } catch (RestClientException ex) {
            log.error("Failed to reach Edge API Gateway at {}: {}", endpoint, ex.getMessage());
            return false;
        }
    }

    /**
     * Sends a stop command to the Edge API Gateway to halt an ongoing dispensing
     * operation. Used in emergency situations or when a feeding plan is deactivated
     * while a dispense is in progress.
     *
     * @param feedingPlanId the ID of the feeding plan whose dispensing should stop
     * @return true if the gateway acknowledged the stop command, false otherwise
     */
    public boolean stopDispensing(UUID feedingPlanId) {
        String endpoint = gatewayBaseUrl + "/api/devices/dispenser/stop";
        log.info("Sending stop-dispense command to Edge Gateway for planId={}", feedingPlanId);

        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> payload = Map.of("feedingPlanId", feedingPlanId.toString());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(endpoint, request, Void.class);

            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("Stop-dispense {} for planId={}", success ? "ACKNOWLEDGED" : "FAILED", feedingPlanId);
            return success;

        } catch (RestClientException ex) {
            log.error("Failed to stop dispensing via Edge Gateway for planId={}: {}", feedingPlanId, ex.getMessage());
            return false;
        }
    }

    /**
     * Checks the connectivity status of the dispenser device via the Edge API Gateway.
     *
     * @param deviceId the hardware identifier of the dispenser device
     * @return true if the device is reachable and operational
     */
    public boolean checkDeviceHealth(String deviceId) {
        String endpoint = gatewayBaseUrl + "/api/devices/" + deviceId + "/health";
        log.debug("Checking device health via Edge Gateway for deviceId={}", deviceId);

        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<Void> response = restTemplate.getForEntity(endpoint, Void.class);
            return response.getStatusCode().is2xxSuccessful();

        } catch (RestClientException ex) {
            log.warn("Device health check failed for deviceId={}: {}", deviceId, ex.getMessage());
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

package com.bluepatitas.bluepatitasbackend.monitoring.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.EnableTrackingCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.EvaluateBreachCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.services.PerimeterAlertService;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.PerimeterAlert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * AlertController
 * <p>
 * REST interface for perimeter alert management within the Monitoring
 * Bounded Context. Exposes endpoints for alert listing, tracking activation,
 * and alert resolution.
 * </p>
 */
@RestController
@RequestMapping("/api/monitoring/alerts")
@RequiredArgsConstructor
@Tag(name = "Monitoring – Alerts", description = "Endpoints for perimeter breach alert management")
public class AlertController {

    private final PerimeterAlertService perimeterAlertService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/monitoring/alerts
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all perimeter alerts registered in the system.
     *
     * @return a list of all PerimeterAlert entries
     */
    @GetMapping
    @Operation(summary = "List all perimeter alerts",
               description = "Returns all perimeter breach alerts regardless of their current status.")
    public ResponseEntity<List<PerimeterAlert>> getAllAlerts() {
        List<PerimeterAlert> alerts = perimeterAlertService.getAllAlerts();
        return ResponseEntity.ok(alerts);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/monitoring/alerts/{targetId}/tracking
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Activates active location tracking for a confirmed perimeter breach alert
     * associated with the given target.
     *
     * @param targetId the UUID of the target whose alert should have tracking enabled
     * @param request  the request body containing the alertId to activate tracking for
     * @return the updated PerimeterAlert with tracking enabled
     */
    @PostMapping("/{targetId}/tracking")
    @Operation(summary = "Enable location tracking",
               description = "Activates real-time location tracking for a confirmed perimeter breach alert.")
    public ResponseEntity<PerimeterAlert> enableTracking(
            @PathVariable UUID targetId,
            @RequestBody EnableTrackingRequest request) {

        EnableTrackingCommand command = new EnableTrackingCommand(request.alertId(), targetId);
        PerimeterAlert updated = perimeterAlertService.enableTracking(command);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/monitoring/alerts/{id}/resolve
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resolves (closes) an existing perimeter breach alert.
     * Typically used when the target has been safely recovered.
     *
     * @param id the UUID of the PerimeterAlert to resolve
     * @return the resolved PerimeterAlert
     */
    @PutMapping("/{id}/resolve")
    @Operation(summary = "Resolve perimeter alert",
               description = "Closes an active perimeter breach alert after the target has been recovered.")
    public ResponseEntity<PerimeterAlert> resolveAlert(@PathVariable UUID id) {
        PerimeterAlert resolved = perimeterAlertService.resolveAlert(id);
        return ResponseEntity.ok(resolved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Request DTOs
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Request body for enabling tracking on an existing alert.
     */
    public record EnableTrackingRequest(UUID alertId) {}
}

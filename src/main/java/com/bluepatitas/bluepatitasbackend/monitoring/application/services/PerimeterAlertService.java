package com.bluepatitas.bluepatitasbackend.monitoring.application.services;

import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.EnableTrackingCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.EvaluateBreachCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.PerimeterAlert;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.AlertRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.valueobjects.LocationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * PerimeterAlertService
 * <p>
 * Application service responsible for detecting perimeter exits, generating
 * automatic breach alerts, and managing the complete location tracking flow.
 * Coordinates domain behaviour on {@link PerimeterAlert} aggregates.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerimeterAlertService {

    private final AlertRepository alertRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Command Handlers (Write Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Evaluates whether the provided coordinates constitute a confirmed perimeter
     * breach. If a breach is detected, creates and persists a new PerimeterAlert
     * and confirms it automatically.
     *
     * @param command the CQRS command carrying target identity and GPS coordinates
     * @return the persisted PerimeterAlert if a breach was confirmed
     */
    @Transactional
    public PerimeterAlert evaluateAndConfirmBreach(EvaluateBreachCommand command) {
        log.info("Evaluating perimeter breach for targetId={} at lat={}, lon={}",
                command.targetId(), command.latitude(), command.longitude());

        LocationContext coordinates = new LocationContext(command.latitude(), command.longitude());

        // Check if there is already an active (unresolved) breach alert for this target
        List<PerimeterAlert> activeAlerts = alertRepository.findAllActiveBreachesByTargetId(command.targetId());
        if (!activeAlerts.isEmpty()) {
            PerimeterAlert existing = activeAlerts.get(0);
            log.info("Active breach alert already exists for targetId={}, alertId={}. Updating coordinates.",
                    command.targetId(), existing.getId());
            existing.updateCoordinates(coordinates);
            return alertRepository.save(existing);
        }

        PerimeterAlert alert = new PerimeterAlert(
                UUID.randomUUID(),
                command.targetId(),
                false,
                coordinates,
                false
        );

        // Apply domain rule: confirm the breach
        alert.confirmBreach();

        PerimeterAlert saved = alertRepository.save(alert);
        log.warn("Perimeter breach CONFIRMED for targetId={}, alertId={}", command.targetId(), saved.getId());
        return saved;
    }

    /**
     * Enables active location tracking for an existing confirmed perimeter alert.
     *
     * @param command the CQRS command specifying the alert to activate tracking for
     * @return the updated PerimeterAlert with tracking enabled
     * @throws NoSuchElementException if no alert is found for the given alertId
     */
    @Transactional
    public PerimeterAlert enableTracking(EnableTrackingCommand command) {
        log.info("Enabling location tracking for alertId={}", command.alertId());

        PerimeterAlert alert = alertRepository.findById(command.alertId())
                .orElseThrow(() -> new NoSuchElementException(
                        "PerimeterAlert not found with id: " + command.alertId()));

        // Apply domain rule: activate tracking
        alert.activateLocationTracking();

        PerimeterAlert updated = alertRepository.save(alert);
        log.info("Location tracking ACTIVATED for alertId={}, targetId={}", updated.getId(), updated.getTargetId());
        return updated;
    }

    /**
     * Resolves an existing perimeter alert, marking it as closed.
     * Typically invoked when the target has been safely recovered.
     *
     * @param alertId the unique identifier of the alert to resolve
     * @return the updated PerimeterAlert in resolved state
     * @throws NoSuchElementException if no alert is found for the given alertId
     */
    @Transactional
    public PerimeterAlert resolveAlert(UUID alertId) {
        log.info("Resolving perimeter alert for alertId={}", alertId);

        PerimeterAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new NoSuchElementException(
                        "PerimeterAlert not found with id: " + alertId));

        // Apply domain rule: resolve the alert
        alert.resolveAlert();

        PerimeterAlert resolved = alertRepository.save(alert);
        log.info("Perimeter alert RESOLVED for alertId={}", resolved.getId());
        return resolved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query Handlers (Read Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all currently active perimeter alerts for a given target.
     *
     * @param targetId the identifier of the monitored target
     * @return a list of active PerimeterAlerts
     */
    @Transactional(readOnly = true)
    public List<PerimeterAlert> getActiveAlertsByTarget(UUID targetId) {
        log.info("Fetching active alerts for targetId={}", targetId);
        return alertRepository.findAllActiveByTargetId(targetId);
    }

    /**
     * Retrieves all perimeter alerts in the system.
     *
     * @return a list of all PerimeterAlerts
     */
    @Transactional(readOnly = true)
    public List<PerimeterAlert> getAllAlerts() {
        log.info("Fetching all perimeter alerts");
        return alertRepository.findAll();
    }

    /**
     * Deletes a perimeter alert from the system.
     *
     * @param alertId the unique identifier of the alert to delete
     */
    @Transactional
    public void deleteAlert(UUID alertId) {
        log.info("Deleting perimeter alert with id={}", alertId);
        alertRepository.deleteById(alertId);
    }
}

package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.valueobjects.LocationContext;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * PerimeterAlert
 * <p>
 * Aggregate Root that represents a perimeter breach alert triggered when a
 * tracked target leaves a predefined safe zone. Manages the lifecycle of the
 * alert from detection through resolution.
 * </p>
 */
@Entity
@Table(name = "monitoring_perimeter_alerts")
@Getter
@NoArgsConstructor
public class PerimeterAlert {

    /** Unique identifier for this perimeter alert. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** The identifier of the target (e.g., pet) that triggered the alert. */
    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    /** Indicates whether the perimeter breach has been confirmed as real. */
    @Column(name = "is_breach_confirmed", nullable = false)
    private Boolean isBreachConfirmed;

    /** The last known geographic coordinates of the target at alert time. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude",  column = @Column(name = "current_latitude",  precision = 10, scale = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "current_longitude", precision = 10, scale = 7))
    })
    private LocationContext currentCoordinates;

    /** Whether active location tracking has been enabled for this alert. */
    @Column(name = "tracking_active", nullable = false)
    private Boolean trackingActive;

    /**
     * Full constructor for creating a PerimeterAlert aggregate.
     *
     * @param id                 unique identifier
     * @param targetId           monitored target's identifier
     * @param isBreachConfirmed  initial confirmation status
     * @param currentCoordinates last known GPS coordinates
     * @param trackingActive     whether tracking is enabled
     */
    public PerimeterAlert(UUID id, UUID targetId, Boolean isBreachConfirmed,
                          LocationContext currentCoordinates, Boolean trackingActive) {
        this.id = id;
        this.targetId = targetId;
        this.isBreachConfirmed = isBreachConfirmed;
        this.currentCoordinates = currentCoordinates;
        this.trackingActive = trackingActive;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Confirms that the detected perimeter breach is real and not a false positive.
     * Once confirmed, the alert becomes eligible for active location tracking.
     *
     * @throws IllegalStateException if the breach is already confirmed
     */
    public void confirmBreach() {
        if (Boolean.TRUE.equals(this.isBreachConfirmed)) {
            throw new IllegalStateException(
                    "Breach for alert [" + this.id + "] is already confirmed.");
        }
        this.isBreachConfirmed = true;
    }

    /**
     * Enables active location tracking for this alert.
     * Tracking can only be activated after the breach has been confirmed.
     *
     * @throws IllegalStateException if the breach has not been confirmed yet
     */
    public void activateLocationTracking() {
        if (!Boolean.TRUE.equals(this.isBreachConfirmed)) {
            throw new IllegalStateException(
                    "Cannot activate tracking: breach for alert [" + this.id + "] is not yet confirmed.");
        }
        this.trackingActive = true;
    }

    /**
     * Updates the last known coordinates of the target.
     *
     * @param coordinates the new GPS location coordinates
     */
    public void updateCoordinates(LocationContext coordinates) {
        this.currentCoordinates = coordinates;
    }

    /**
     * Resolves and closes this alert, deactivating location tracking.
     * Typically called when the target has been safely recovered or
     * returned to the perimeter zone.
     *
     * @throws IllegalStateException if the alert was never breach-confirmed
     */
    public void resolveAlert() {
        if (!Boolean.TRUE.equals(this.isBreachConfirmed)) {
            throw new IllegalStateException(
                    "Cannot resolve alert [" + this.id + "]: breach was never confirmed.");
        }
        this.trackingActive = false;
        this.isBreachConfirmed = false;
    }
}

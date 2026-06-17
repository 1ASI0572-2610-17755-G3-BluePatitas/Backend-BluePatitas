package com.bluepatitas.bluepatitasbackend.veterinary.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * VeterinaryAlertReview
 * <p>
 * Domain Entity (not an Aggregate Root) that represents a veterinarian's
 * review of a monitoring perimeter alert. Tracks which veterinarian reviewed
 * a specific alert and what resolution status was applied.
 * </p>
 *
 * <p>Lifecycle:
 * <pre>
 *   PENDING ──► REVIEWED
 *           └─► DISMISSED
 * </pre>
 * </p>
 */
@Entity
@Table(name = "veterinary_alert_reviews")
@Getter
@NoArgsConstructor
public class VeterinaryAlertReview {

    /** Unique identifier for this alert review. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** The monitoring perimeter alert being reviewed. */
    @Column(name = "alert_id", nullable = false)
    private UUID alertId;

    /** The veterinarian who performed the review. */
    @Column(name = "veterinarian_id")
    private UUID veterinarianId;

    /**
     * The resolution status of this review.
     * Values: "PENDING", "REVIEWED", "DISMISSED".
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * Full constructor for creating a new VeterinaryAlertReview in PENDING state.
     *
     * @param id      unique identifier for the review
     * @param alertId the monitoring alert being reviewed
     */
    public VeterinaryAlertReview(UUID id, UUID alertId) {
        if (id == null)      throw new IllegalArgumentException("VeterinaryAlertReview id must not be null.");
        if (alertId == null) throw new IllegalArgumentException("AlertId must not be null.");

        this.id             = id;
        this.alertId        = alertId;
        this.veterinarianId = null;
        this.status         = "PENDING";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Records the review of this alert by a veterinarian, applying the given
     * resolution status.
     * <p>
     * Valid status values are {@code "REVIEWED"} and {@code "DISMISSED"}.
     * A PENDING review can only be reviewed once.
     * </p>
     *
     * @param veterinarianId the UUID of the veterinarian performing the review
     * @param status         the resolution status to apply ("REVIEWED" or "DISMISSED")
     * @throws IllegalStateException    if this review has already been processed
     * @throws IllegalArgumentException if veterinarianId is null or status is invalid
     */
    public void reviewAlert(UUID veterinarianId, String status) {
        if (!"PENDING".equals(this.status)) {
            throw new IllegalStateException(
                    "VeterinaryAlertReview [" + this.id + "] has already been processed. Current status: " + this.status);
        }
        if (veterinarianId == null) {
            throw new IllegalArgumentException("VeterinarianId must not be null when reviewing an alert.");
        }
        if (!"REVIEWED".equals(status) && !"DISMISSED".equals(status)) {
            throw new IllegalArgumentException(
                    "Invalid review status: '" + status + "'. Allowed values: REVIEWED, DISMISSED.");
        }
        this.veterinarianId = veterinarianId;
        this.status         = status;
    }

    /**
     * Returns whether this review is still awaiting a veterinarian's action.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
}

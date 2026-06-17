package com.bluepatitas.bluepatitasbackend.feeding.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FeedingEvent
 * <p>
 * Domain Entity (not an Aggregate Root) that represents a single feeding
 * execution event associated with a {@code FeedingPlan}. Tracks the scheduled
 * vs. actual execution time and the outcome of each dispenser activation.
 * </p>
 *
 * <p>The lifecycle of a FeedingEvent is:
 * <pre>
 *   PENDING ──► EXECUTED
 *           └─► FAILED
 * </pre>
 * </p>
 */
@Entity
@Table(name = "feeding_events")
@Getter
@NoArgsConstructor
public class FeedingEvent {

    /** Unique identifier for this feeding event. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** The feeding plan that generated this event. */
    @Column(name = "feeding_plan_id", nullable = false)
    private UUID feedingPlanId;

    /** The moment at which this feeding was scheduled to occur. */
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    /** The actual moment the dispenser was triggered. Null until executed. */
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    /**
     * Outcome status of this feeding event.
     * Values: "PENDING", "EXECUTED", "FAILED".
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * Full constructor for creating a new FeedingEvent in PENDING state.
     *
     * @param id            unique identifier
     * @param feedingPlanId the associated feeding plan
     * @param scheduledAt   the scheduled execution time
     */
    public FeedingEvent(UUID id, UUID feedingPlanId, LocalDateTime scheduledAt) {
        if (id == null)            throw new IllegalArgumentException("FeedingEvent id must not be null.");
        if (feedingPlanId == null) throw new IllegalArgumentException("FeedingPlanId must not be null.");
        if (scheduledAt == null)   throw new IllegalArgumentException("ScheduledAt must not be null.");

        this.id            = id;
        this.feedingPlanId = feedingPlanId;
        this.scheduledAt   = scheduledAt;
        this.status        = "PENDING";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Records the successful execution of this feeding event by the dispenser.
     * Sets the execution timestamp to now and transitions the status to EXECUTED.
     *
     * @throws IllegalStateException if the event is not in PENDING state
     */
    public void markAsExecuted() {
        if (!"PENDING".equals(this.status)) {
            throw new IllegalStateException(
                    "FeedingEvent [" + this.id + "] cannot be marked as EXECUTED. Current status: " + this.status);
        }
        this.executedAt = LocalDateTime.now();
        this.status     = "EXECUTED";
    }

    /**
     * Records the failure of a feeding event, typically due to a dispenser error
     * or connectivity issue with the IoT Edge API Gateway.
     *
     * @throws IllegalStateException if the event is not in PENDING state
     */
    public void markAsFailed() {
        if (!"PENDING".equals(this.status)) {
            throw new IllegalStateException(
                    "FeedingEvent [" + this.id + "] cannot be marked as FAILED. Current status: " + this.status);
        }
        this.status = "FAILED";
    }

    /**
     * Returns whether this event was successfully executed.
     *
     * @return true if status is EXECUTED
     */
    public boolean isExecuted() {
        return "EXECUTED".equals(this.status);
    }

    /**
     * Returns whether this event is still awaiting execution.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
}

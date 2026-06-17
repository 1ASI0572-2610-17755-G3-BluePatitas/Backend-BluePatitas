package com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates;

import com.bluepatitas.bluepatitasbackend.feeding.domain.model.enumerations.FeedingPlanStatus;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.DietType;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.FeedingSchedule;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.FoodAmount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * FeedingPlan
 * <p>
 * Aggregate Root of the Feeding Bounded Context. Represents a complete dietary
 * and scheduling plan for a specific animal in the BluePatitas platform.
 * Encapsulates all domain behaviour related to diet configuration, food portion
 * management, schedule definition, and plan lifecycle transitions.
 * </p>
 */
@Entity
@Table(name = "feeding_plans")
@Getter
@NoArgsConstructor
public class FeedingPlan {

    /** Unique identifier for this feeding plan. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** The animal this feeding plan belongs to. */
    @Column(name = "animal_id", nullable = false)
    private UUID animalId;

    /** Dietary classification and nutritional guidance for this plan. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name",             column = @Column(name = "diet_name",             nullable = false, length = 100)),
            @AttributeOverride(name = "nutritionalNotes", column = @Column(name = "diet_nutritional_notes", columnDefinition = "TEXT"))
    })
    private DietType dietType;

    /** Quantity and unit of food to dispense per feeding event. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "quantity", column = @Column(name = "food_quantity", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "unit",     column = @Column(name = "food_unit",     nullable = false, length = 30))
    })
    private FoodAmount foodAmount;

    /** Temporal configuration: slots per day, scheduled times, and tolerance window. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "timesPerDay",      column = @Column(name = "schedule_times_per_day",  nullable = false)),
            @AttributeOverride(name = "scheduledTimes",   column = @Column(name = "schedule_times",          nullable = false, length = 200)),
            @AttributeOverride(name = "toleranceMinutes", column = @Column(name = "schedule_tolerance_min",  nullable = false))
    })
    private FeedingSchedule schedule;

    /** Current lifecycle state of the plan. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeedingPlanStatus status;

    /** Timestamp when this feeding plan was first created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp of the last modification to this feeding plan. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Factory Method
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Factory method that creates a new FeedingPlan in DRAFT state.
     * <p>
     * All newly created plans start as {@link FeedingPlanStatus#DRAFT} and
     * must be explicitly activated before the dispenser device will execute them.
     * </p>
     *
     * @param id         unique identifier for the plan
     * @param animalId   the animal this plan belongs to
     * @param dietType   dietary classification and nutritional notes
     * @param foodAmount quantity and unit of food per feeding
     * @param schedule   temporal configuration (slots, times, tolerance)
     * @return a new FeedingPlan in DRAFT state
     */
    public static FeedingPlan create(UUID id, UUID animalId, DietType dietType,
                                     FoodAmount foodAmount, FeedingSchedule schedule) {
        if (id == null)       throw new IllegalArgumentException("FeedingPlan id must not be null.");
        if (animalId == null) throw new IllegalArgumentException("AnimalId must not be null.");

        FeedingPlan plan = new FeedingPlan();
        plan.id         = id;
        plan.animalId   = animalId;
        plan.dietType   = dietType;
        plan.foodAmount = foodAmount;
        plan.schedule   = schedule;
        plan.status     = FeedingPlanStatus.DRAFT;
        plan.createdAt  = LocalDateTime.now();
        plan.updatedAt  = LocalDateTime.now();
        return plan;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the dietary classification of this feeding plan.
     *
     * @param newDietType the new diet type (must be non-null)
     * @throws IllegalArgumentException if newDietType is null
     */
    public void updateDiet(DietType newDietType) {
        if (newDietType == null) {
            throw new IllegalArgumentException("DietType must not be null when updating diet.");
        }
        this.dietType  = newDietType;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the food portion amount for this feeding plan.
     *
     * @param newFoodAmount the new food quantity and unit (must be non-null)
     * @throws IllegalArgumentException if newFoodAmount is null
     */
    public void updateFoodAmount(FoodAmount newFoodAmount) {
        if (newFoodAmount == null) {
            throw new IllegalArgumentException("FoodAmount must not be null when updating portions.");
        }
        this.foodAmount = newFoodAmount;
        this.updatedAt  = LocalDateTime.now();
    }

    /**
     * Updates the feeding schedule for this plan.
     *
     * @param newSchedule the new feeding schedule (must be non-null)
     * @throws IllegalArgumentException if newSchedule is null
     */
    public void updateSchedule(FeedingSchedule newSchedule) {
        if (newSchedule == null) {
            throw new IllegalArgumentException("FeedingSchedule must not be null when updating schedule.");
        }
        this.schedule  = newSchedule;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates this feeding plan, enabling the dispenser device to execute
     * feedings according to the configured schedule.
     *
     * @throws IllegalStateException if the plan is already ACTIVE
     */
    public void activate() {
        if (FeedingPlanStatus.ACTIVE.equals(this.status)) {
            throw new IllegalStateException(
                    "FeedingPlan [" + this.id + "] is already ACTIVE.");
        }
        this.status    = FeedingPlanStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivates this feeding plan, pausing all dispenser executions.
     * The plan data is preserved and the plan can be reactivated later.
     *
     * @throws IllegalStateException if the plan is already INACTIVE or still in DRAFT
     */
    public void deactivate() {
        if (!FeedingPlanStatus.ACTIVE.equals(this.status)) {
            throw new IllegalStateException(
                    "FeedingPlan [" + this.id + "] can only be deactivated from ACTIVE state. Current: " + this.status);
        }
        this.status    = FeedingPlanStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Determines whether this feeding plan should trigger a dispenser execution
     * at the given date and time.
     * <p>
     * Returns {@code true} only if the plan is {@link FeedingPlanStatus#ACTIVE}
     * and the provided time matches one of the configured schedule slots within
     * the tolerance window.
     * </p>
     *
     * @param dateTime the moment to evaluate (typically {@code LocalDateTime.now()})
     * @return true if a feeding event should be triggered at the given time
     */
    public boolean canBeExecutedAt(LocalDateTime dateTime) {
        if (!FeedingPlanStatus.ACTIVE.equals(this.status)) {
            return false;
        }
        return this.schedule.matchesTime(dateTime.toLocalTime());
    }
}

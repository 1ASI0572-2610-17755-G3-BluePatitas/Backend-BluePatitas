package com.bluepatitas.bluepatitasbackend.feeding.domain.model.enumerations;

/**
 * FeedingPlanStatus
 * <p>
 * Enumeration representing the lifecycle state of a {@code FeedingPlan}
 * within the Feeding Bounded Context.
 * </p>
 */
public enum FeedingPlanStatus {

    /**
     * The plan has been created but is not yet active. No feedings will
     * be triggered while the plan is in DRAFT state.
     */
    DRAFT,

    /**
     * The plan is active and the dispenser device will execute feedings
     * according to the defined schedule.
     */
    ACTIVE,

    /**
     * The plan has been explicitly deactivated. Feedings are paused
     * but the plan data is preserved for future reactivation.
     */
    INACTIVE
}

package com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * FeedingSchedule
 * <p>
 * Value Object that defines the temporal configuration for feeding events
 * within a {@code FeedingPlan}. Encapsulates how many times per day the
 * animal should be fed, at which specific times, and the tolerance window
 * around each scheduled slot.
 * </p>
 *
 * @param timesPerDay      how many feeding events occur per day
 * @param scheduledTimes   comma-separated time slots in "HH:mm" format
 *                         (e.g., {@code "08:00,13:00,19:00"})
 * @param toleranceMinutes how many minutes before or after each slot the
 *                         execution is still considered on-time
 */
@Embeddable
public record FeedingSchedule(
        Integer timesPerDay,
        String scheduledTimes,
        Integer toleranceMinutes
) {

    /**
     * Compact constructor that validates domain invariants.
     *
     * @throws IllegalArgumentException if timesPerDay is null or less than 1,
     *                                  or if scheduledTimes is null or blank
     */
    public FeedingSchedule {
        if (timesPerDay == null || timesPerDay < 1) {
            throw new IllegalArgumentException("Feeding frequency must be at least 1 time per day.");
        }
        if (scheduledTimes == null || scheduledTimes.isBlank()) {
            throw new IllegalArgumentException("Scheduled times must not be blank. Provide HH:mm slots separated by commas.");
        }
        if (toleranceMinutes == null || toleranceMinutes < 0) {
            toleranceMinutes = 10; // default tolerance window
        }
    }

    /**
     * Determines whether the provided time falls within the tolerance window
     * of any of this schedule's configured time slots.
     * <p>
     * Used by {@code FeedingPlan#canBeExecutedAt(LocalDateTime)} to decide
     * whether a feeding event should be triggered at the current moment.
     * </p>
     *
     * @param time the time to evaluate
     * @return true if the time matches any scheduled slot within the tolerance
     */
    public boolean matchesTime(LocalTime time) {
        if (time == null) return false;
        int tolerance = (toleranceMinutes != null) ? toleranceMinutes : 10;
        try {
            return Arrays.stream(scheduledTimes.split(","))
                    .map(String::trim)
                    .map(LocalTime::parse)
                    .anyMatch(slot -> {
                        long diff = Math.abs(ChronoUnit.MINUTES.between(slot, time));
                        return diff <= tolerance;
                    });
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the schedule as a human-readable summary.
     *
     * @return e.g. "3x/day at [08:00, 13:00, 19:00] (±10 min)"
     */
    @Override
    public String toString() {
        String slots = "[" + scheduledTimes.replace(",", ", ") + "]";
        return timesPerDay + "x/day at " + slots + " (±" + toleranceMinutes + " min)";
    }
}

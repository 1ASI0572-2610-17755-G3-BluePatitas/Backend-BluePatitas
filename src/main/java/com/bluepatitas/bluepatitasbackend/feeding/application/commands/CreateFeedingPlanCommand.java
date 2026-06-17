package com.bluepatitas.bluepatitasbackend.feeding.application.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * CreateFeedingPlanCommand
 * <p>
 * CQRS Write-side command that instructs the application to create a new
 * feeding plan for a registered animal. The plan is created in DRAFT state
 * and must be explicitly activated before dispenser events are triggered.
 * </p>
 *
 * @param animalId           the UUID of the animal this plan belongs to
 * @param dietName           the dietary category name (e.g., "High-Protein")
 * @param nutritionalNotes   optional veterinary notes for this diet
 * @param foodQuantity       the amount of food per feeding event
 * @param foodUnit           the unit of food measurement (e.g., "grams", "cups")
 * @param timesPerDay        how many feeding events per day
 * @param scheduledTimes     comma-separated "HH:mm" time slots (e.g., "08:00,13:00,19:00")
 * @param toleranceMinutes   tolerance window in minutes around each scheduled slot
 */
public record CreateFeedingPlanCommand(
        UUID animalId,
        String dietName,
        String nutritionalNotes,
        BigDecimal foodQuantity,
        String foodUnit,
        Integer timesPerDay,
        String scheduledTimes,
        Integer toleranceMinutes
) {
}

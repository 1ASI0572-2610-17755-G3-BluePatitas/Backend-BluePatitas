package com.bluepatitas.bluepatitasbackend.feeding.application.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * UpdateFeedingPlanCommand
 * <p>
 * CQRS Write-side command that instructs the application to update one or
 * more aspects of an existing feeding plan. All fields except {@code planId}
 * are optional — passing {@code null} for a field means "no change".
 * </p>
 *
 * @param planId             the unique identifier of the plan to update
 * @param dietName           new dietary category name, or null to keep current
 * @param nutritionalNotes   new nutritional notes, or null to keep current
 * @param foodQuantity       new food quantity per event, or null to keep current
 * @param foodUnit           new food unit, or null to keep current
 * @param timesPerDay        new feeding frequency per day, or null to keep current
 * @param scheduledTimes     new comma-separated "HH:mm" slots, or null to keep current
 * @param toleranceMinutes   new tolerance window, or null to keep current
 */
public record UpdateFeedingPlanCommand(
        UUID planId,
        String dietName,
        String nutritionalNotes,
        BigDecimal foodQuantity,
        String foodUnit,
        Integer timesPerDay,
        String scheduledTimes,
        Integer toleranceMinutes
) {
}

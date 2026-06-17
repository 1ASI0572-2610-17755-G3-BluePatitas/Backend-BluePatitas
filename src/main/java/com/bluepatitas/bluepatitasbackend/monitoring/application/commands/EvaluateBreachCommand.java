package com.bluepatitas.bluepatitasbackend.monitoring.application.commands;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * EvaluateBreachCommand
 * <p>
 * CQRS Write-side command that instructs the application to evaluate
 * whether a detected perimeter breach should be confirmed as real, based
 * on the target's current geographic coordinates.
 * </p>
 *
 * @param targetId  the identifier of the target that may have breached the perimeter
 * @param latitude  the current latitude of the target
 * @param longitude the current longitude of the target
 */
public record EvaluateBreachCommand(
        UUID targetId,
        BigDecimal latitude,
        BigDecimal longitude
) {
}

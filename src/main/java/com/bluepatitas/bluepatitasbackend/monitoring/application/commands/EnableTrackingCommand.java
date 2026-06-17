package com.bluepatitas.bluepatitasbackend.monitoring.application.commands;

import java.util.UUID;

/**
 * EnableTrackingCommand
 * <p>
 * CQRS Write-side command that instructs the application to enable
 * active location tracking for an existing, confirmed perimeter alert.
 * </p>
 *
 * @param alertId  the unique identifier of the PerimeterAlert to enable tracking for
 * @param targetId the identifier of the target to actively track
 */
public record EnableTrackingCommand(
        UUID alertId,
        UUID targetId
) {
}

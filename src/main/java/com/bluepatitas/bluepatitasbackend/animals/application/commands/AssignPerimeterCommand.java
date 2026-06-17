package com.bluepatitas.bluepatitasbackend.animals.application.commands;

import java.util.UUID;

/**
 * AssignPerimeterCommand
 * <p>
 * CQRS Write-side command that instructs the application to relocate an
 * animal to a specific monitoring perimeter zone. Passing a null
 * {@code perimeterId} removes the animal from its current zone.
 * </p>
 *
 * @param animalId    the unique identifier of the animal to relocate
 * @param perimeterId the UUID of the target perimeter zone, or null to unassign
 */
public record AssignPerimeterCommand(
        UUID animalId,
        UUID perimeterId
) {
}

package com.bluepatitas.bluepatitasbackend.animals.application.commands;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;

import java.util.UUID;

/**
 * UpdateHealthCommand
 * <p>
 * CQRS Write-side command that instructs the application to update the
 * health condition of an existing animal in the platform.
 * </p>
 *
 * @param animalId  the unique identifier of the animal to update
 * @param newStatus the new health status to apply to the animal
 */
public record UpdateHealthCommand(
        UUID animalId,
        HealthStatus newStatus
) {
}

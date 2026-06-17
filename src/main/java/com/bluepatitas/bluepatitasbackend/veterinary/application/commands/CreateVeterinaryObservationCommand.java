package com.bluepatitas.bluepatitasbackend.veterinary.application.commands;

import java.util.UUID;

/**
 * CreateVeterinaryObservationCommand
 * <p>
 * CQRS Write-side command that instructs the application to create a new
 * veterinary observation for an animal. The observation is created without
 * a recommendation — it can be added later via
 * {@link AddVeterinaryRecommendationCommand}.
 * </p>
 *
 * @param animalId       the UUID of the animal being observed
 * @param veterinarianId the UUID of the veterinarian creating the observation
 * @param description    the clinical description of the observation
 */
public record CreateVeterinaryObservationCommand(
        UUID animalId,
        UUID veterinarianId,
        String description
) {
}

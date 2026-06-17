package com.bluepatitas.bluepatitasbackend.veterinary.application.commands;

import java.util.UUID;

/**
 * AddVeterinaryRecommendationCommand
 * <p>
 * CQRS Write-side command that instructs the application to attach a dietary
 * or treatment recommendation to an existing veterinary observation.
 * </p>
 *
 * <p>After the recommendation is persisted, the application layer publishes a
 * {@code VeterinaryFeedingRecommendationCreatedEvent} so that the Feeding
 * Bounded Context can automatically generate a new feeding plan.</p>
 *
 * @param observationId  the UUID of the observation to update
 * @param recommendation the dietary or treatment recommendation text
 */
public record AddVeterinaryRecommendationCommand(
        UUID observationId,
        String recommendation
) {
}

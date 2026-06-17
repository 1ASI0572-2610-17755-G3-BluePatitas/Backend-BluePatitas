package com.bluepatitas.bluepatitasbackend.veterinary.domain.model.events;

import java.util.UUID;

/**
 * VeterinaryFeedingRecommendationCreatedEvent
 * <p>
 * Domain Event published when a veterinarian creates a dietary recommendation
 * for a specific animal within the Veterinary Bounded Context.
 * </p>
 *
 * <p>This event crosses the context boundary into the Feeding Bounded Context,
 * where it is consumed by the
 * {@code VeterinaryFeedingRecommendationCreatedEventHandler} to automatically
 * generate a new {@code FeedingPlan} for the animal.</p>
 *
 * <p><b>Event flow:</b>
 * <pre>
 *   VeterinaryObservation.createFeedingRecommendation()
 *       → VeterinaryObservationCommandHandler publishes via ApplicationEventPublisher
 *           → Feeding BC: VeterinaryFeedingRecommendationCreatedEventHandler.on(event)
 * </pre>
 * </p>
 *
 * @param animalId       the UUID of the animal for whom the recommendation was issued
 * @param recommendation the full dietary recommendation text from the veterinarian
 */
public record VeterinaryFeedingRecommendationCreatedEvent(
        UUID animalId,
        String recommendation
) {
}

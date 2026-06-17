package com.bluepatitas.bluepatitasbackend.feeding.application.services;

import com.bluepatitas.bluepatitasbackend.feeding.application.commands.CreateFeedingPlanCommand;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates.FeedingPlan;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.events.VeterinaryFeedingRecommendationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * VeterinaryFeedingRecommendationCreatedEventHandler
 * <p>
 * Application-layer event handler that listens for
 * {@link VeterinaryFeedingRecommendationCreatedEvent} domain events published
 * by the Veterinary Bounded Context. When a veterinarian issues a new dietary
 * recommendation, this handler automatically translates it into a
 * {@link CreateFeedingPlanCommand} and delegates its execution to the
 * {@link FeedingPlanCommandHandler}.
 * </p>
 *
 * <p><b>Anti-Corruption Layer note:</b> This handler is the integration
 * point between the Veterinary and Feeding contexts. It maps the Veterinary
 * domain event to Feeding domain model concepts, preventing veterinary
 * concepts from leaking into the Feeding Bounded Context's own domain.</p>
 *
 * <p><b>Event bus:</b> Uses Spring's built-in {@code ApplicationEventPublisher}
 * mechanism via {@link EventListener}. To migrate to a message broker
 * (e.g., RabbitMQ, Kafka), replace {@link EventListener} with the
 * corresponding broker consumer annotation and deserialize the payload
 * into a {@link VeterinaryFeedingRecommendationCreatedEvent}.</p>
 *
 * <p><b>Default schedule:</b> Since {@link VeterinaryFeedingRecommendationCreatedEvent}
 * only carries {@code animalId} and {@code recommendation}, this handler applies
 * sensible defaults for food quantity, schedule, and tolerance. These can be
 * extended in the event payload when the Veterinary context evolves.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VeterinaryFeedingRecommendationCreatedEventHandler {

    private final FeedingPlanCommandHandler feedingPlanCommandHandler;

    /**
     * Handles a veterinary feeding recommendation event by automatically
     * creating a new feeding plan in DRAFT state for the recommended animal.
     * <p>
     * Applies sensible defaults for scheduling and food amounts since the
     * current event payload only carries the diet text. A veterinarian or
     * caretaker can update the plan details via the Feeding API after creation.
     * </p>
     *
     * @param event the domain event published by {@code VeterinaryObservationCommandHandler}
     */
    @EventListener
    public void on(VeterinaryFeedingRecommendationCreatedEvent event) {
        log.info("Received VeterinaryFeedingRecommendationCreatedEvent for animalId={}, recommendation='{}'",
                event.animalId(), event.recommendation());

        // Default schedule: twice a day at 08:00 and 18:00, 10-minute tolerance
        CreateFeedingPlanCommand command = new CreateFeedingPlanCommand(
                event.animalId(),
                event.recommendation(),         // diet name from the vet's recommendation
                "Recommended by veterinarian",  // default nutritional note
                new BigDecimal("200"),           // default 200 grams per feeding
                "grams",
                2,                              // 2x per day
                "08:00,18:00",
                10
        );

        FeedingPlan created = feedingPlanCommandHandler.handle(command);
        log.info("FeedingPlan AUTO-CREATED from veterinary recommendation: planId={}, animalId={}",
                created.getId(), created.getAnimalId());
    }
}

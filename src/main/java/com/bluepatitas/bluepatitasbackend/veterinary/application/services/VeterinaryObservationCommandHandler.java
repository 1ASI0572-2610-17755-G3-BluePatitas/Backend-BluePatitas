package com.bluepatitas.bluepatitasbackend.veterinary.application.services;

import com.bluepatitas.bluepatitasbackend.veterinary.application.commands.AddVeterinaryRecommendationCommand;
import com.bluepatitas.bluepatitasbackend.veterinary.application.commands.CreateVeterinaryObservationCommand;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates.VeterinaryObservation;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.events.VeterinaryFeedingRecommendationCreatedEvent;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.repositories.VeterinaryObservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * VeterinaryObservationCommandHandler
 * <p>
 * Application service that processes CQRS write-side commands for the
 * {@link VeterinaryObservation} aggregate root. Responsible for:
 * <ul>
 *   <li>Creating new observations via {@link CreateVeterinaryObservationCommand}</li>
 *   <li>Adding dietary recommendations via {@link AddVeterinaryRecommendationCommand}</li>
 *   <li>Publishing {@link VeterinaryFeedingRecommendationCreatedEvent} to the
 *       Spring application event bus after a recommendation is added</li>
 *   <li>Read-side query operations</li>
 * </ul>
 * </p>
 *
 * <p><b>Cross-BC integration:</b> After persisting a recommendation, this handler
 * delegates the domain event to {@code ApplicationEventPublisher}. The Feeding
 * Bounded Context's event handler ({@code VeterinaryFeedingRecommendationCreatedEventHandler})
 * will pick it up and auto-create a FeedingPlan for the animal.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VeterinaryObservationCommandHandler {

    private final VeterinaryObservationRepository observationRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ─────────────────────────────────────────────────────────────────────────
    // Command Handlers (Write Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new {@link VeterinaryObservation} with no recommendation set.
     * <p>
     * Delegates to the aggregate's factory method, ensuring all domain
     * invariants are enforced before persisting.
     * </p>
     *
     * @param command the CQRS command carrying observation creation data
     * @return the newly persisted VeterinaryObservation
     */
    @Transactional
    public VeterinaryObservation handle(CreateVeterinaryObservationCommand command) {
        log.info("Creating VeterinaryObservation for animalId={}, veterinarianId={}",
                command.animalId(), command.veterinarianId());

        VeterinaryObservation observation = VeterinaryObservation.create(
                UUID.randomUUID(),
                command.animalId(),
                command.veterinarianId(),
                command.description()
        );

        VeterinaryObservation saved = observationRepository.save(observation);
        log.info("VeterinaryObservation CREATED with id={}", saved.getId());
        return saved;
    }

    /**
     * Adds a recommendation to an existing {@link VeterinaryObservation} and
     * publishes a {@link VeterinaryFeedingRecommendationCreatedEvent} to trigger
     * automatic feeding plan creation in the Feeding Bounded Context.
     * <p>
     * The event is published <em>after</em> the transaction commits, ensuring
     * the observation is already persisted when the Feeding BC handles the event.
     * </p>
     *
     * @param command the CQRS command specifying the observation and recommendation text
     * @return the updated VeterinaryObservation
     * @throws NoSuchElementException if no observation is found with the given id
     */
    @Transactional
    public VeterinaryObservation handle(AddVeterinaryRecommendationCommand command) {
        log.info("Adding recommendation to observationId={}", command.observationId());

        VeterinaryObservation observation = observationRepository.findById(command.observationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "VeterinaryObservation not found with id: " + command.observationId()));

        // Apply domain behaviour: set recommendation
        observation.addRecommendation(command.recommendation());

        // Persist updated aggregate
        VeterinaryObservation updated = observationRepository.save(observation);
        log.info("Recommendation ADDED to observationId={}", updated.getId());

        // Create and publish cross-BC domain event
        VeterinaryFeedingRecommendationCreatedEvent event = observation.createFeedingRecommendation();
        eventPublisher.publishEvent(event);
        log.info("VeterinaryFeedingRecommendationCreatedEvent PUBLISHED for animalId={}", event.animalId());

        return updated;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query Handlers (Read Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a single veterinary observation by its unique identifier.
     *
     * @param id the UUID of the observation
     * @return the VeterinaryObservation aggregate
     * @throws NoSuchElementException if no observation is found with the given id
     */
    @Transactional(readOnly = true)
    public VeterinaryObservation getObservationById(UUID id) {
        log.info("Fetching VeterinaryObservation by id={}", id);
        return observationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "VeterinaryObservation not found with id: " + id));
    }

    /**
     * Retrieves all veterinary observations associated with a specific animal.
     *
     * @param animalId the UUID of the animal
     * @return a list of VeterinaryObservations for that animal
     */
    @Transactional(readOnly = true)
    public List<VeterinaryObservation> getObservationsByAnimalId(UUID animalId) {
        log.info("Fetching veterinary observations for animalId={}", animalId);
        return observationRepository.findAllByAnimalId(animalId);
    }

    /**
     * Retrieves all veterinary observations in the system.
     *
     * @return a list of all VeterinaryObservation aggregates
     */
    @Transactional(readOnly = true)
    public List<VeterinaryObservation> getAllObservations() {
        log.info("Fetching all veterinary observations");
        return observationRepository.findAll();
    }
}

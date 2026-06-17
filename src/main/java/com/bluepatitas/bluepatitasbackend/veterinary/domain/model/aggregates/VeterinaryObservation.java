package com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates;

import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.events.VeterinaryFeedingRecommendationCreatedEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * VeterinaryObservation
 * <p>
 * Aggregate Root of the Veterinary Bounded Context. Represents a clinical
 * observation recorded by a veterinarian for a specific animal. Encapsulates
 * all domain behaviour related to description management, dietary
 * recommendations, and cross-context event creation.
 * </p>
 */
@Entity
@Table(name = "veterinary_observations")
@Getter
@NoArgsConstructor
public class VeterinaryObservation {

    /** Unique identifier for this veterinary observation. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** The animal this observation refers to. */
    @Column(name = "animal_id", nullable = false)
    private UUID animalId;

    /** The veterinarian who created this observation. */
    @Column(name = "veterinarian_id", nullable = false)
    private UUID veterinarianId;

    /** Clinical description of the animal's condition at the time of observation. */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Dietary and treatment recommendation issued by the veterinarian.
     * Null until explicitly added via {@link #addRecommendation(String)}.
     */
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    /** Timestamp when this observation was first recorded. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Factory Method
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Factory method that creates a new VeterinaryObservation.
     * <p>
     * A new observation is created without a recommendation — the veterinarian
     * adds it later via {@link #addRecommendation(String)}.
     * </p>
     *
     * @param id              unique identifier
     * @param animalId        the animal being observed
     * @param veterinarianId  the veterinarian recording the observation
     * @param description     the clinical description of the observation
     * @return a new VeterinaryObservation with no recommendation set
     * @throws IllegalArgumentException if any required argument is null or blank
     */
    public static VeterinaryObservation create(UUID id, UUID animalId,
                                               UUID veterinarianId, String description) {
        if (id == null)                              throw new IllegalArgumentException("VeterinaryObservation id must not be null.");
        if (animalId == null)                        throw new IllegalArgumentException("AnimalId must not be null.");
        if (veterinarianId == null)                  throw new IllegalArgumentException("VeterinarianId must not be null.");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description must not be blank.");

        VeterinaryObservation obs = new VeterinaryObservation();
        obs.id             = id;
        obs.animalId       = animalId;
        obs.veterinarianId = veterinarianId;
        obs.description    = description;
        obs.recommendation = null;
        obs.createdAt      = LocalDateTime.now();
        return obs;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Adds or replaces the dietary and treatment recommendation for this
     * veterinary observation.
     * <p>
     * Once a recommendation is added, the observation is eligible to publish
     * a {@link VeterinaryFeedingRecommendationCreatedEvent} via
     * {@link #createFeedingRecommendation()}.
     * </p>
     *
     * @param recommendation the recommendation text (must be non-blank)
     * @throws IllegalArgumentException if the recommendation is null or blank
     */
    public void addRecommendation(String recommendation) {
        if (recommendation == null || recommendation.isBlank()) {
            throw new IllegalArgumentException("Recommendation must not be blank.");
        }
        this.recommendation = recommendation;
    }

    /**
     * Creates a {@link VeterinaryFeedingRecommendationCreatedEvent} from the
     * current recommendation, signalling the Feeding Bounded Context to
     * generate a new feeding plan for this animal.
     * <p>
     * The returned event must be published by the application layer via
     * {@code ApplicationEventPublisher} — the aggregate itself does not
     * perform any publishing to keep the domain framework-free.
     * </p>
     *
     * @return the domain event carrying the animalId and recommendation
     * @throws IllegalStateException if no recommendation has been set yet
     */
    public VeterinaryFeedingRecommendationCreatedEvent createFeedingRecommendation() {
        if (recommendation == null || recommendation.isBlank()) {
            throw new IllegalStateException(
                    "Cannot create feeding recommendation event for observation ["
                    + this.id + "]: no recommendation has been added yet.");
        }
        return new VeterinaryFeedingRecommendationCreatedEvent(this.animalId, this.recommendation);
    }
}

package com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.valueobjects.SpeciesInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Animal
 * <p>
 * Aggregate Root of the Animals Bounded Context. Represents a pet or animal
 * registered in the BluePatitas platform. Encapsulates all domain behaviour
 * related to profile management, health condition tracking, and perimeter
 * assignment, enforcing business invariants through dedicated domain methods.
 * </p>
 */
@Entity
@Table(name = "animals_animal")
@Getter
@NoArgsConstructor
public class Animal {

    /** Unique identifier for this animal. */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** The common name given to the animal (e.g., "Max", "Luna"). */
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    /** Embedded value object capturing taxonomic and age information. */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "species",            column = @Column(name = "species",              nullable = false, length = 80)),
            @AttributeOverride(name = "breed",              column = @Column(name = "breed",                nullable = false, length = 120)),
            @AttributeOverride(name = "estimatedAgeMonths", column = @Column(name = "estimated_age_months", nullable = false))
    })
    private SpeciesInfo speciesDetails;

    /** Current clinical state of the animal. */
    @Enumerated(EnumType.STRING)
    @Column(name = "health_condition", nullable = false, length = 30)
    private HealthStatus healthCondition;

    /**
     * The UUID of the monitoring perimeter zone this animal is currently
     * assigned to. Nullable when the animal has not yet been placed in a zone.
     */
    @Column(name = "assigned_perimeter_id")
    private UUID assignedPerimeterId;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Full constructor for creating a new Animal aggregate.
     *
     * @param id                  unique identifier (must be non-null)
     * @param name                the animal's name (must be non-blank)
     * @param speciesDetails      taxonomic and age value object
     * @param healthCondition     initial health status
     * @param assignedPerimeterId the perimeter zone UUID, or null if unassigned
     */
    public Animal(UUID id, String name, SpeciesInfo speciesDetails,
                  HealthStatus healthCondition, UUID assignedPerimeterId) {
        if (id == null) {
            throw new IllegalArgumentException("Animal id must not be null.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Animal name must not be blank.");
        }
        this.id = id;
        this.name = name;
        this.speciesDetails = speciesDetails;
        this.healthCondition = healthCondition;
        this.assignedPerimeterId = assignedPerimeterId;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Domain Behaviour
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the species and breed profile of this animal.
     * <p>
     * Replaces the existing {@link SpeciesInfo} with the provided one, allowing
     * corrections to species, breed, or estimated age data.
     * </p>
     *
     * @param newSpeciesInfo the updated species information (must be non-null)
     * @throws IllegalArgumentException if the provided speciesInfo is null
     */
    public void updateBaseProfile(SpeciesInfo newSpeciesInfo) {
        if (newSpeciesInfo == null) {
            throw new IllegalArgumentException("SpeciesInfo must not be null when updating profile.");
        }
        this.speciesDetails = newSpeciesInfo;
    }

    /**
     * Registers a new health condition for this animal.
     * <p>
     * Updates the clinical state. Transitioning to {@link HealthStatus#CRITICAL}
     * from any state is always permitted to avoid blocking emergency updates.
     * </p>
     *
     * @param newStatus the new health status to apply (must be non-null)
     * @throws IllegalArgumentException if the provided status is null
     */
    public void registerHealthCondition(HealthStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Health status must not be null.");
        }
        this.healthCondition = newStatus;
    }

    /**
     * Assigns this animal to a specific monitoring perimeter zone.
     * <p>
     * Passing {@code null} effectively removes the animal from any zone,
     * marking it as unassigned.
     * </p>
     *
     * @param perimeterId the UUID of the target perimeter zone, or null to unassign
     */
    public void relocateToPerimeter(UUID perimeterId) {
        this.assignedPerimeterId = perimeterId;
    }
}

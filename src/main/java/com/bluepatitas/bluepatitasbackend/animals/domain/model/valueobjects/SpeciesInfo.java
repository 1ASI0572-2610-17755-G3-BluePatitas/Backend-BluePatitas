package com.bluepatitas.bluepatitasbackend.animals.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * SpeciesInfo
 * <p>
 * Value Object that encapsulates the taxonomic and age-related details of an
 * animal within the Animals Bounded Context. Being immutable, any change to
 * species information results in a new instance, preserving domain invariants.
 * </p>
 *
 * @param species           the common species name (e.g., "Dog", "Cat")
 * @param breed             the specific breed within the species (e.g., "Golden Retriever")
 * @param estimatedAgeMonths the estimated age of the animal expressed in months
 */
@Embeddable
public record SpeciesInfo(
        String species,
        String breed,
        Integer estimatedAgeMonths
) {

    /**
     * Compact constructor that validates domain invariants on construction.
     *
     * @throws IllegalArgumentException if species or breed is blank, or age is negative
     */
    public SpeciesInfo {
        if (species == null || species.isBlank()) {
            throw new IllegalArgumentException("Species name must not be blank.");
        }
        if (breed == null || breed.isBlank()) {
            throw new IllegalArgumentException("Breed must not be blank.");
        }
        if (estimatedAgeMonths == null || estimatedAgeMonths < 0) {
            throw new IllegalArgumentException("Estimated age in months must be zero or positive.");
        }
    }

    /**
     * Returns a human-readable description combining species and breed.
     *
     * @return formatted species description, e.g. "Golden Retriever (Dog)"
     */
    public String toDisplayName() {
        return breed + " (" + species + ")";
    }
}

package com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

/**
 * DietType
 * <p>
 * Value Object that describes the dietary classification and any
 * relevant nutritional guidance associated with a {@code FeedingPlan}.
 * Being immutable, any modification produces a new instance, preserving
 * domain invariants within the Feeding Bounded Context.
 * </p>
 *
 * @param name             the dietary category name (e.g., "High-Protein", "Low-Fat", "Renal Diet")
 * @param nutritionalNotes free-text nutritional guidance or veterinary indications for this diet
 */
@Embeddable
public record DietType(
        String name,
        String nutritionalNotes
) {

    /**
     * Compact constructor that validates domain invariants on construction.
     *
     * @throws IllegalArgumentException if the diet name is null or blank
     */
    public DietType {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Diet type name must not be blank.");
        }
    }

    /**
     * Constructs a DietType with a name but without specific nutritional notes.
     *
     * @param name the dietary category name
     */
    public DietType(String name) {
        this(name, null);
    }

    /**
     * Returns a human-readable label combining name and notes when present.
     *
     * @return formatted description, e.g. "Renal Diet — Low phosphorus, restricted protein"
     */
    public String toDisplayLabel() {
        if (nutritionalNotes == null || nutritionalNotes.isBlank()) {
            return name;
        }
        return name + " — " + nutritionalNotes;
    }
}

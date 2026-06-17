package com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/**
 * FoodAmount
 * <p>
 * Value Object that encapsulates the quantity and unit of food to be
 * dispensed per feeding event within a {@code FeedingPlan}.
 * Immutable by design — any change produces a new instance.
 * </p>
 *
 * @param quantity the amount of food to dispense per feeding
 * @param unit     the unit of measurement (e.g., "grams", "kg", "cups")
 */
@Embeddable
public record FoodAmount(
        BigDecimal quantity,
        String unit
) {

    /**
     * Compact constructor that validates domain invariants.
     *
     * @throws IllegalArgumentException if quantity is null, zero or negative,
     *                                  or if unit is null or blank
     */
    public FoodAmount {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Food quantity must be greater than zero.");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Food unit must not be blank.");
        }
    }

    /**
     * Returns whether this food amount exceeds a given threshold quantity.
     *
     * @param threshold the maximum recommended quantity in the same unit
     * @return true if this amount exceeds the threshold
     */
    public boolean exceeds(BigDecimal threshold) {
        if (threshold == null) return false;
        return this.quantity.compareTo(threshold) > 0;
    }

    /**
     * Returns a human-readable representation of this food amount.
     *
     * @return formatted string, e.g. "250 grams"
     */
    @Override
    public String toString() {
        return quantity.stripTrailingZeros().toPlainString() + " " + unit;
    }
}

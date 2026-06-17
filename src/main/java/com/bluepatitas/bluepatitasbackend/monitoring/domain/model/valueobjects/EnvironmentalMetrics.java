package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * EnvironmentalMetrics
 * <p>
 * Value Object that encapsulates environmental threshold configuration
 * for temperature and humidity readings within the Monitoring context.
 * </p>
 */
@Embeddable
public record EnvironmentalMetrics(
        BigDecimal maxTemperature,
        BigDecimal minTemperature,
        BigDecimal maxHumidity,
        BigDecimal minHumidity
) {

    /**
     * Validates that the provided temperature reading is within the acceptable range.
     *
     * @param temperature the ambient temperature to validate
     * @return true if the temperature is within bounds, false otherwise
     */
    public boolean isTemperatureWithinBounds(BigDecimal temperature) {
        if (temperature == null) return false;
        return temperature.compareTo(minTemperature) >= 0
                && temperature.compareTo(maxTemperature) <= 0;
    }

    /**
     * Validates that the provided humidity reading is within the acceptable range.
     *
     * @param humidity the ambient humidity to validate
     * @return true if the humidity is within bounds, false otherwise
     */
    public boolean isHumidityWithinBounds(BigDecimal humidity) {
        if (humidity == null) return false;
        return humidity.compareTo(minHumidity) >= 0
                && humidity.compareTo(maxHumidity) <= 0;
    }

    /**
     * Returns a default environmental metrics configuration with standard thresholds.
     */
    public static EnvironmentalMetrics defaults() {
        return new EnvironmentalMetrics(
                new BigDecimal("35.0"),
                new BigDecimal("10.0"),
                new BigDecimal("80.0"),
                new BigDecimal("20.0")
        );
    }
}

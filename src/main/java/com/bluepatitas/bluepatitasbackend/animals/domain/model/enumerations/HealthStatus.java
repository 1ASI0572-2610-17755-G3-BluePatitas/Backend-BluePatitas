package com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations;

/**
 * HealthStatus
 * <p>
 * Enumeration representing the health condition of an animal registered
 * in the BluePatitas platform. Used by the {@code Animal} aggregate root
 * to express clinical state transitions.
 * </p>
 */
public enum HealthStatus {

    /**
     * The animal shows no signs of illness and all vital indicators
     * are within normal ranges.
     */
    HEALTHY,

    /**
     * The animal is currently undergoing a veterinary treatment protocol.
     * Regular check-ups are expected.
     */
    IN_TREATMENT,

    /**
     * The animal is in a critical health state and requires immediate or
     * intensive veterinary intervention.
     */
    CRITICAL,

    /**
     * The animal is being monitored for potential health issues but has not
     * yet received a definitive diagnosis.
     */
    UNDER_OBSERVATION
}

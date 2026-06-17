package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.PerimeterAlert;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AlertRepository
 * <p>
 * Domain repository interface for {@link PerimeterAlert} aggregates.
 * Defines the persistence contract for alert management operations
 * without coupling the domain to any specific storage technology.
 * </p>
 */
public interface AlertRepository {

    /**
     * Persists a new or updated PerimeterAlert.
     *
     * @param alert the perimeter alert to save
     * @return the saved perimeter alert
     */
    PerimeterAlert save(PerimeterAlert alert);

    /**
     * Retrieves a perimeter alert by its unique identifier.
     *
     * @param id the unique identifier of the alert
     * @return an Optional containing the alert if found
     */
    Optional<PerimeterAlert> findById(UUID id);

    /**
     * Retrieves all active (tracking-enabled) alerts for a given target.
     *
     * @param targetId the identifier of the monitored target
     * @return a list of active perimeter alerts for the target
     */
    List<PerimeterAlert> findAllActiveByTargetId(UUID targetId);

    /**
     * Retrieves all perimeter alerts in the system, regardless of status.
     *
     * @return a list of all perimeter alerts
     */
    List<PerimeterAlert> findAll();

    /**
     * Deletes a perimeter alert by its unique identifier.
     *
     * @param id the unique identifier of the alert to delete
     */
    void deleteById(UUID id);

    /**
     * Checks whether an alert exists for a specific target.
     *
     * @param targetId the identifier of the monitored target
     * @return true if at least one alert exists for that target
     */
    boolean existsByTargetId(UUID targetId);
}

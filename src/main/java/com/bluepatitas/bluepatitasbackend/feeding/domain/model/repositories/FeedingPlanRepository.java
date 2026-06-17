package com.bluepatitas.bluepatitasbackend.feeding.domain.model.repositories;

import com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates.FeedingPlan;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.enumerations.FeedingPlanStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * FeedingPlanRepository
 * <p>
 * Domain repository interface for {@link FeedingPlan} aggregate roots.
 * Defines the persistence contract for feeding plan management without
 * coupling the domain layer to any specific storage technology.
 * </p>
 */
public interface FeedingPlanRepository {

    /**
     * Persists a new or updated FeedingPlan aggregate.
     *
     * @param plan the feeding plan to save
     * @return the saved FeedingPlan
     */
    FeedingPlan save(FeedingPlan plan);

    /**
     * Retrieves a feeding plan by its unique identifier.
     *
     * @param id the unique identifier of the feeding plan
     * @return an Optional containing the plan if found
     */
    Optional<FeedingPlan> findById(UUID id);

    /**
     * Retrieves all feeding plans associated with a specific animal.
     *
     * @param animalId the UUID of the animal
     * @return a list of feeding plans for the given animal
     */
    List<FeedingPlan> findAllByAnimalId(UUID animalId);

    /**
     * Retrieves all feeding plans with a specific lifecycle status.
     *
     * @param status the plan status to filter by (DRAFT, ACTIVE, INACTIVE)
     * @return a list of feeding plans matching the given status
     */
    List<FeedingPlan> findAllByStatus(FeedingPlanStatus status);

    /**
     * Retrieves all feeding plans registered in the platform.
     *
     * @return a list of all FeedingPlan aggregates
     */
    List<FeedingPlan> findAll();

    /**
     * Checks whether any active feeding plan exists for a given animal.
     *
     * @param animalId the UUID of the animal
     * @return true if at least one ACTIVE plan exists for that animal
     */
    boolean existsActiveByAnimalId(UUID animalId);

    /**
     * Deletes a feeding plan by its unique identifier.
     *
     * @param id the unique identifier of the feeding plan to delete
     */
    void deleteById(UUID id);
}

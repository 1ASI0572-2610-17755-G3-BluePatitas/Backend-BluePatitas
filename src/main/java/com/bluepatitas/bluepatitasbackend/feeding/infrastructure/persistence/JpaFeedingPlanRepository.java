package com.bluepatitas.bluepatitasbackend.feeding.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates.FeedingPlan;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.enumerations.FeedingPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JpaFeedingPlanRepository
 * <p>
 * Spring Data JPA derived-query repository for {@link FeedingPlan} entities.
 * This interface is an infrastructure detail and must NOT be used directly
 * outside of {@link SqlFeedingPlanRepository}.
 * </p>
 */
public interface JpaFeedingPlanRepository extends JpaRepository<FeedingPlan, UUID> {

    /**
     * Fetches all feeding plans for a specific animal.
     *
     * @param animalId the UUID of the animal
     * @return list of feeding plans associated to that animal
     */
    List<FeedingPlan> findAllByAnimalId(UUID animalId);

    /**
     * Fetches all feeding plans in a specific lifecycle status.
     *
     * @param status the plan status to filter by
     * @return list of feeding plans with that status
     */
    List<FeedingPlan> findAllByStatus(FeedingPlanStatus status);

    /**
     * Checks whether any ACTIVE feeding plan exists for a given animal.
     *
     * @param animalId the UUID of the animal
     * @param status   the status to check for (expected: ACTIVE)
     * @return true if at least one plan with that status exists for the animal
     */
    boolean existsByAnimalIdAndStatus(UUID animalId, FeedingPlanStatus status);
}

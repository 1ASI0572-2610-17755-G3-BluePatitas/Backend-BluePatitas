package com.bluepatitas.bluepatitasbackend.feeding.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates.FeedingPlan;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.enumerations.FeedingPlanStatus;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.repositories.FeedingPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SqlFeedingPlanRepository
 * <p>
 * Infrastructure implementation of {@link FeedingPlanRepository} backed by a
 * relational database (MySQL) via Spring Data JPA. Provides transactional
 * persistence for the {@link FeedingPlan} aggregate root.
 * </p>
 *
 * <p><b>Design notes:</b>
 * <ul>
 *   <li>Uses a dedicated {@link JpaFeedingPlanRepository} Spring Data interface
 *       to keep the domain repository interface free of JPA concerns.</li>
 *   <li>An index on {@code (animal_id, status)} is recommended to efficiently
 *       serve the most common filtered queries.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SqlFeedingPlanRepository implements FeedingPlanRepository {

    private final JpaFeedingPlanRepository jpaFeedingPlanRepository;

    @Override
    public FeedingPlan save(FeedingPlan plan) {
        log.debug("Persisting FeedingPlan id={} for animalId={}", plan.getId(), plan.getAnimalId());
        return jpaFeedingPlanRepository.save(plan);
    }

    @Override
    public Optional<FeedingPlan> findById(UUID id) {
        log.debug("Fetching FeedingPlan by id={}", id);
        return jpaFeedingPlanRepository.findById(id);
    }

    @Override
    public List<FeedingPlan> findAllByAnimalId(UUID animalId) {
        log.debug("Fetching feeding plans for animalId={}", animalId);
        return jpaFeedingPlanRepository.findAllByAnimalId(animalId);
    }

    @Override
    public List<FeedingPlan> findAllByStatus(FeedingPlanStatus status) {
        log.debug("Fetching feeding plans with status={}", status);
        return jpaFeedingPlanRepository.findAllByStatus(status);
    }

    @Override
    public List<FeedingPlan> findAll() {
        log.debug("Fetching all feeding plans");
        return jpaFeedingPlanRepository.findAll();
    }

    @Override
    public boolean existsActiveByAnimalId(UUID animalId) {
        return jpaFeedingPlanRepository.existsByAnimalIdAndStatus(animalId, FeedingPlanStatus.ACTIVE);
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting FeedingPlan id={}", id);
        jpaFeedingPlanRepository.deleteById(id);
    }
}

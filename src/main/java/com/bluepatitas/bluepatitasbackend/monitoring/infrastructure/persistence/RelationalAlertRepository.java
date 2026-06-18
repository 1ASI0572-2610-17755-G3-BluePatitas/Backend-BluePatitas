package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.PerimeterAlert;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RelationalAlertRepository
 * <p>
 * Infrastructure implementation of {@link AlertRepository} backed by a
 * relational database (MySQL) via Spring Data JPA. Optimized for transactional
 * alert persistence and consistent status updates.
 * </p>
 *
 * <p><b>Design notes:</b>
 * <ul>
 *   <li>Uses a dedicated {@link JpaAlertRepository} Spring Data interface to keep
 *       the domain repository interface free of JPA framework concerns.</li>
 *   <li>An index on {@code (target_id, tracking_active)} is recommended for
 *       efficient queries on active alerts.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RelationalAlertRepository implements AlertRepository {

    private final JpaAlertRepository jpaAlertRepository;

    @Override
    public PerimeterAlert save(PerimeterAlert alert) {
        log.debug("Persisting PerimeterAlert id={} for targetId={}", alert.getId(), alert.getTargetId());
        return jpaAlertRepository.save(alert);
    }

    @Override
    public Optional<PerimeterAlert> findById(UUID id) {
        log.debug("Fetching PerimeterAlert by id={}", id);
        return jpaAlertRepository.findById(id);
    }

    @Override
    public List<PerimeterAlert> findAllActiveByTargetId(UUID targetId) {
        log.debug("Fetching active alerts for targetId={}", targetId);
        return jpaAlertRepository.findAllByTargetIdAndTrackingActiveTrue(targetId);
    }

    @Override
    public List<PerimeterAlert> findAllActiveBreachesByTargetId(UUID targetId) {
        log.debug("Fetching breach-confirmed alerts for targetId={}", targetId);
        return jpaAlertRepository.findAllByTargetIdAndIsBreachConfirmedTrue(targetId);
    }

    @Override
    public List<PerimeterAlert> findAll() {
        log.debug("Fetching all perimeter alerts");
        return jpaAlertRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting PerimeterAlert id={}", id);
        jpaAlertRepository.deleteById(id);
    }

    @Override
    public boolean existsByTargetId(UUID targetId) {
        return jpaAlertRepository.existsByTargetId(targetId);
    }
}

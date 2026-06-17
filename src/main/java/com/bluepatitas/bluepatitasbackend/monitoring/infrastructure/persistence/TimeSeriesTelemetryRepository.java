package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.TelemetryRecord;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TimeSeriesTelemetryRepository
 * <p>
 * Infrastructure implementation of {@link TelemetryRepository}, optimized for
 * sequential time-series reads. Delegates persistence to a Spring Data JPA
 * repository and applies ordering strategies suited for chronological queries.
 * </p>
 *
 * <p><b>Optimization notes:</b>
 * <ul>
 *   <li>Results are fetched ordered by {@code recorded_at ASC} at the database level.</li>
 *   <li>A composite index on {@code (target_id, recorded_at)} is assumed in the schema
 *       to support efficient range scans.</li>
 *   <li>Pagination should be applied when dealing with large time-series windows.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TimeSeriesTelemetryRepository implements TelemetryRepository {

    private final JpaTelemetryRepository jpaTelemetryRepository;

    @Override
    public TelemetryRecord save(TelemetryRecord record) {
        log.debug("Persisting TelemetryRecord id={} for targetId={}", record.getId(), record.getTargetId());
        return jpaTelemetryRepository.save(record);
    }

    @Override
    public Optional<TelemetryRecord> findById(UUID id) {
        log.debug("Fetching TelemetryRecord by id={}", id);
        return jpaTelemetryRepository.findById(id);
    }

    @Override
    public List<TelemetryRecord> findAllByTargetIdOrderByRecordedAtAsc(UUID targetId) {
        log.debug("Fetching time-series TelemetryRecords for targetId={}", targetId);
        return jpaTelemetryRepository.findAllByTargetIdOrderByRecordedAtAsc(targetId);
    }

    @Override
    public Optional<TelemetryRecord> findLatestByTargetId(UUID targetId) {
        log.debug("Fetching latest TelemetryRecord for targetId={}", targetId);
        return jpaTelemetryRepository.findTopByTargetIdOrderByRecordedAtDesc(targetId);
    }

    @Override
    public boolean existsByTargetId(UUID targetId) {
        return jpaTelemetryRepository.existsByTargetId(targetId);
    }
}

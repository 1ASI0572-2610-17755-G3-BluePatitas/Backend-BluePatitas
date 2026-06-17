package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.TelemetryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JpaTelemetryRepository
 * <p>
 * Spring Data JPA derived-query repository for {@link TelemetryRecord} entities.
 * This interface is an infrastructure detail and must NOT be used directly
 * outside of {@link TimeSeriesTelemetryRepository}.
 * </p>
 */
public interface JpaTelemetryRepository extends JpaRepository<TelemetryRecord, UUID> {

    /**
     * Fetches all telemetry records for a target ordered chronologically ascending.
     * Leverages the (target_id, recorded_at) composite index for performance.
     *
     * @param targetId the monitored target's UUID
     * @return ordered list of telemetry records
     */
    List<TelemetryRecord> findAllByTargetIdOrderByRecordedAtAsc(UUID targetId);

    /**
     * Fetches the single most recent telemetry record for a target.
     *
     * @param targetId the monitored target's UUID
     * @return an Optional containing the latest record
     */
    Optional<TelemetryRecord> findTopByTargetIdOrderByRecordedAtDesc(UUID targetId);

    /**
     * Checks whether any telemetry record exists for a given target.
     *
     * @param targetId the monitored target's UUID
     * @return true if at least one record exists
     */
    boolean existsByTargetId(UUID targetId);
}

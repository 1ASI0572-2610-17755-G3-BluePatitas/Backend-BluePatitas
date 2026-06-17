package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.TelemetryRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TelemetryRepository
 * <p>
 * Domain repository interface for {@link TelemetryRecord} aggregates.
 * Defines the contract for persistence operations without coupling
 * the domain to any specific infrastructure technology.
 * </p>
 */
public interface TelemetryRepository {

    /**
     * Persists a new or updated TelemetryRecord.
     *
     * @param record the telemetry record to save
     * @return the saved telemetry record
     */
    TelemetryRecord save(TelemetryRecord record);

    /**
     * Retrieves a telemetry record by its unique identifier.
     *
     * @param id the unique identifier of the record
     * @return an Optional containing the record if found
     */
    Optional<TelemetryRecord> findById(UUID id);

    /**
     * Retrieves all telemetry records associated with a given target,
     * ordered chronologically (oldest first) to support time-series analysis.
     *
     * @param targetId the identifier of the monitored target
     * @return a list of telemetry records for the target
     */
    List<TelemetryRecord> findAllByTargetIdOrderByRecordedAtAsc(UUID targetId);

    /**
     * Retrieves the most recent telemetry record for a given target.
     *
     * @param targetId the identifier of the monitored target
     * @return an Optional containing the latest record if any exists
     */
    Optional<TelemetryRecord> findLatestByTargetId(UUID targetId);

    /**
     * Checks whether at least one telemetry record exists for the given target.
     *
     * @param targetId the identifier of the monitored target
     * @return true if at least one record exists
     */
    boolean existsByTargetId(UUID targetId);
}

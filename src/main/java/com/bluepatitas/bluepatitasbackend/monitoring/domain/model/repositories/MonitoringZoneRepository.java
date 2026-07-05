package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MonitoringZoneRepository domain interface.
 */
public interface MonitoringZoneRepository {
    MonitoringZone save(MonitoringZone monitoringZone);
    Optional<MonitoringZone> findById(UUID id);
    Optional<MonitoringZone> findByTargetId(UUID targetId);
    List<MonitoringZone> findAll();
    List<MonitoringZone> findAllByShelterId(UUID shelterId);
    void deleteById(UUID id);
}

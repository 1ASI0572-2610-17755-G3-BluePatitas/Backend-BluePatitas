package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JpaMonitoringZoneRepository
 */
public interface JpaMonitoringZoneRepository extends JpaRepository<MonitoringZone, UUID> {
    List<MonitoringZone> findAllByShelterId(UUID shelterId);
    Optional<MonitoringZone> findByTargetId(UUID targetId);
}

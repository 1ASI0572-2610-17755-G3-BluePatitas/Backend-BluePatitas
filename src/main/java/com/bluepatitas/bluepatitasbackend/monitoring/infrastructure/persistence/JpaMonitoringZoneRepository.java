package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * JpaMonitoringZoneRepository
 */
public interface JpaMonitoringZoneRepository extends JpaRepository<MonitoringZone, UUID> {
}

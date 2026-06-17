package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.MonitoringZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RelationalMonitoringZoneRepository
 */
@Repository
@RequiredArgsConstructor
public class RelationalMonitoringZoneRepository implements MonitoringZoneRepository {

    private final JpaMonitoringZoneRepository jpaMonitoringZoneRepository;

    @Override
    public MonitoringZone save(MonitoringZone monitoringZone) {
        return jpaMonitoringZoneRepository.save(monitoringZone);
    }

    @Override
    public Optional<MonitoringZone> findById(UUID id) {
        return jpaMonitoringZoneRepository.findById(id);
    }

    @Override
    public Optional<MonitoringZone> findByTargetId(UUID targetId) {
        return jpaMonitoringZoneRepository.findByTargetId(targetId);
    }

    @Override
    public List<MonitoringZone> findAll() {
        return jpaMonitoringZoneRepository.findAll();
    }

    @Override
    public List<MonitoringZone> findAllByShelterId(UUID shelterId) {
        return jpaMonitoringZoneRepository.findAllByShelterId(shelterId);
    }
}

package com.bluepatitas.bluepatitasbackend.monitoring.application.services;

import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.CreateMonitoringZoneCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.CreateShelterCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.UpdateShelterDetailsCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.queries.GetMonitoringZonesQuery;
import com.bluepatitas.bluepatitasbackend.monitoring.application.queries.GetShelterSettingsQuery;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.MonitoringZoneRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.ShelterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ShelterMonitoringService
 * <p>
 * Application service managing Shelter and MonitoringZone aggregates.
 * Handles configuration settings and zone lists for the dashboard.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShelterMonitoringService {

    private final ShelterRepository shelterRepository;
    private final MonitoringZoneRepository monitoringZoneRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Shelter Command Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Shelter createShelter(CreateShelterCommand command) {
        log.info("Creating shelter: name='{}'", command.name());
        Shelter shelter = new Shelter(
                UUID.randomUUID(),
                command.name(),
                command.city(),
                command.address(),
                command.administrator(),
                command.phone(),
                command.email()
        );
        return shelterRepository.save(shelter);
    }

    @Transactional
    public Shelter updateShelter(UpdateShelterDetailsCommand command) {
        log.info("Updating shelter settings: name='{}'", command.name());
        Optional<Shelter> existing = shelterRepository.findFirst();
        Shelter shelter;
        if (existing.isPresent()) {
            shelter = existing.get();
            shelter.setName(command.name());
            shelter.setCity(command.city());
            shelter.setAddress(command.address());
            shelter.setAdministrator(command.administrator());
            shelter.setPhone(command.phone());
            shelter.setEmail(command.email());
        } else {
            log.info("No shelter found, creating a new one on update.");
            shelter = new Shelter(
                    UUID.randomUUID(),
                    command.name(),
                    command.city(),
                    command.address(),
                    command.administrator(),
                    command.phone(),
                    command.email()
            );
        }
        return shelterRepository.save(shelter);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MonitoringZone Command Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public MonitoringZone createMonitoringZone(CreateMonitoringZoneCommand command) {
        log.info("Creating monitoring zone: name='{}'", command.name());
        UUID zoneId = UUID.randomUUID();
        MonitoringZone zone = new MonitoringZone(
                zoneId,
                command.targetId() != null ? command.targetId() : UUID.randomUUID(),
                command.name(),
                command.temperatureC(),
                command.humidity(),
                command.status(),
                command.animalCount(),
                command.cameraEnabled(),
                command.imageUrl()
        );
        return monitoringZoneRepository.save(zone);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<Shelter> getShelterSettings(GetShelterSettingsQuery query) {
        log.info("Fetching shelter settings.");
        return shelterRepository.findFirst();
    }

    @Transactional(readOnly = true)
    public List<MonitoringZone> getMonitoringZones(GetMonitoringZonesQuery query) {
        log.info("Fetching all monitoring zones.");
        return monitoringZoneRepository.findAll();
    }
}

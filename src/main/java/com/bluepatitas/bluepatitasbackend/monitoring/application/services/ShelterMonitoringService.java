package com.bluepatitas.bluepatitasbackend.monitoring.application.services;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.aggregates.User;
import com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ShelterMonitoringService
 * <p>
 * Application service managing Shelter and MonitoringZone aggregates.
 * Handles configuration settings and zone lists for the dashboard with multi-tenant isolation.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShelterMonitoringService {

    private final ShelterRepository shelterRepository;
    private final MonitoringZoneRepository monitoringZoneRepository;
    private final UserRepository userRepository;

    private Optional<User> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shelter Command Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Shelter createShelter(CreateShelterCommand command) {
        log.info("Creating shelter: name='{}'", command.name());
        UUID shelterId = UUID.randomUUID();
        Shelter shelter = new Shelter(
                shelterId,
                command.name(),
                command.city(),
                command.address(),
                command.administrator(),
                command.phone(),
                command.email()
        );
        Shelter saved = shelterRepository.save(shelter);

        getCurrentUser().ifPresent(user -> {
            user.setShelterId(shelterId);
            userRepository.save(user);
            log.info("Associated new shelter id={} with current user email='{}'", shelterId, user.getEmail());
        });

        return saved;
    }

    @Transactional
    public Shelter updateShelter(UpdateShelterDetailsCommand command) {
        log.info("Updating shelter settings: name='{}'", command.name());
        Optional<User> currentUserOpt = getCurrentUser();

        if (currentUserOpt.isPresent()) {
            User user = currentUserOpt.get();
            if (user.getShelterId() != null) {
                Optional<Shelter> existing = shelterRepository.findById(user.getShelterId());
                if (existing.isPresent()) {
                    Shelter shelter = existing.get();
                    shelter.setName(command.name());
                    shelter.setCity(command.city());
                    shelter.setAddress(command.address());
                    shelter.setAdministrator(command.administrator());
                    shelter.setPhone(command.phone());
                    shelter.setEmail(command.email());
                    return shelterRepository.save(shelter);
                }
            }
        }

        // Fallback or Initial setup: create a new shelter
        UUID shelterId = UUID.randomUUID();
        Shelter shelter = new Shelter(
                shelterId,
                command.name(),
                command.city(),
                command.address(),
                command.administrator(),
                command.phone(),
                command.email()
        );
        Shelter saved = shelterRepository.save(shelter);

        currentUserOpt.ifPresent(user -> {
            user.setShelterId(shelterId);
            userRepository.save(user);
            log.info("Associated shelter id={} with current user on update.", shelterId);
        });

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MonitoringZone Command Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public MonitoringZone createMonitoringZone(CreateMonitoringZoneCommand command) {
        log.info("Creating monitoring zone: name='{}'", command.name());
        User user = getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));
        if (user.getShelterId() == null) {
            throw new IllegalStateException("User is not associated with any shelter.");
        }

        UUID zoneId = UUID.randomUUID();
        MonitoringZone zone = new MonitoringZone(
                zoneId,
                user.getShelterId(),
                command.targetId() != null ? command.targetId() : UUID.randomUUID(),
                command.name(),
                command.temperatureC(),
                command.humidity(),
                command.status(),
                command.animalCount(),
                command.cameraEnabled(),
                command.imageUrl(),
                command.minTemperatureC(),
                command.maxTemperatureC()
        );
        return monitoringZoneRepository.save(zone);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query Handlers
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<Shelter> getShelterSettings(GetShelterSettingsQuery query) {
        log.info("Fetching shelter settings.");
        return getCurrentUser()
                .filter(user -> user.getShelterId() != null)
                .flatMap(user -> shelterRepository.findById(user.getShelterId()));
    }

    @Transactional(readOnly = true)
    public List<MonitoringZone> getMonitoringZones(GetMonitoringZonesQuery query) {
        log.info("Fetching all monitoring zones.");
        return getCurrentUser()
                .filter(user -> user.getShelterId() != null)
                .map(user -> monitoringZoneRepository.findAllByShelterId(user.getShelterId()))
                .orElse(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public List<MonitoringZone> getMonitoringZonesWithoutAuth() {
        log.info("Fetching all monitoring zones without authentication context.");
        return monitoringZoneRepository.findAll();
    }
}

package com.bluepatitas.bluepatitasbackend.monitoring.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.CreateMonitoringZoneCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.CreateShelterCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.commands.UpdateShelterDetailsCommand;
import com.bluepatitas.bluepatitasbackend.monitoring.application.queries.GetMonitoringZonesQuery;
import com.bluepatitas.bluepatitasbackend.monitoring.application.queries.GetShelterSettingsQuery;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;
import com.bluepatitas.bluepatitasbackend.monitoring.application.services.ShelterMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * ShelterMonitoringController
 * <p>
 * REST Controller for managing Shelter and MonitoringZone resources.
 * </p>
 */
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Tag(name = "Monitoring – Shelter & Zones", description = "Endpoints for shelter settings and monitoring zones management")
public class ShelterMonitoringController {

    private final ShelterMonitoringService shelterMonitoringService;

    // ─────────────────────────────────────────────────────────────────────────
    // Shelter endpoints
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/shelter")
    @Operation(summary = "Get shelter settings", description = "Retrieves the active shelter configuration if one exists.")
    public ResponseEntity<Shelter> getShelterSettings() {
        return shelterMonitoringService.getShelterSettings(new GetShelterSettingsQuery())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok().build()); // Returns 200 OK with empty body (null)
    }

    @PostMapping("/shelter")
    @Operation(summary = "Create shelter settings", description = "Creates a new shelter configuration.")
    public ResponseEntity<Shelter> createShelter(@Valid @RequestBody ShelterRequest request) {
        CreateShelterCommand command = new CreateShelterCommand(
                request.name(),
                request.city(),
                request.address(),
                request.administrator(),
                request.phone(),
                request.email()
        );
        Shelter saved = shelterMonitoringService.createShelter(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/shelter")
    @Operation(summary = "Update shelter settings", description = "Updates or creates the shelter configuration settings.")
    public ResponseEntity<Shelter> updateShelter(@Valid @RequestBody ShelterRequest request) {
        // Use a random or dummy UUID if not provided/valid, since service.updateShelter will use findFirst()
        UUID shelterId = UUID.randomUUID();
        UpdateShelterDetailsCommand command = new UpdateShelterDetailsCommand(
                shelterId,
                request.name(),
                request.city(),
                request.address(),
                request.administrator(),
                request.phone(),
                request.email()
        );
        Shelter updated = shelterMonitoringService.updateShelter(command);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Monitoring Zone endpoints
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/zones")
    @Operation(summary = "Get all monitoring zones", description = "Retrieves a list of all defined monitoring zones.")
    public ResponseEntity<List<MonitoringZone>> getMonitoringZones() {
        List<MonitoringZone> zones = shelterMonitoringService.getMonitoringZones(new GetMonitoringZonesQuery());
        return ResponseEntity.ok(zones);
    }

    @PostMapping("/zones")
    @Operation(summary = "Create a monitoring zone", description = "Registers a new monitoring zone.")
    public ResponseEntity<MonitoringZone> createMonitoringZone(@Valid @RequestBody ZoneRequest request) {
        CreateMonitoringZoneCommand command = new CreateMonitoringZoneCommand(
                request.targetId() != null ? UUID.fromString(request.targetId()) : null,
                request.name(),
                request.temperatureC(),
                request.humidity(),
                request.status() != null ? request.status() : "Active",
                request.animalCount() != null ? request.animalCount() : 0,
                request.cameraEnabled() != null ? request.cameraEnabled() : true,
                request.imageUrl()
        );
        MonitoringZone saved = shelterMonitoringService.createMonitoringZone(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Request DTOs
    // ─────────────────────────────────────────────────────────────────────────

    public record ShelterRequest(
            String name,
            String city,
            String address,
            String administrator,
            String phone,
            String email
    ) {}

    public record ZoneRequest(
            String targetId,
            String name,
            Double temperatureC,
            Double humidity,
            String status,
            Integer animalCount,
            Boolean cameraEnabled,
            String imageUrl
    ) {}
}

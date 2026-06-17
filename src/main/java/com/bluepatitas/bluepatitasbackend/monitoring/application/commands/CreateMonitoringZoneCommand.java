package com.bluepatitas.bluepatitasbackend.monitoring.application.commands;

import java.util.UUID;

/**
 * CreateMonitoringZoneCommand
 */
public record CreateMonitoringZoneCommand(
        UUID targetId,
        String name,
        Double temperatureC,
        Double humidity,
        String status,
        Integer animalCount,
        Boolean cameraEnabled,
        String imageUrl
) {
}

package com.bluepatitas.bluepatitasbackend.monitoring.application.commands;

import java.util.UUID;

/**
 * UpdateShelterDetailsCommand
 */
public record UpdateShelterDetailsCommand(
        UUID id,
        String name,
        String city,
        String address,
        String administrator,
        String phone,
        String email
) {
}

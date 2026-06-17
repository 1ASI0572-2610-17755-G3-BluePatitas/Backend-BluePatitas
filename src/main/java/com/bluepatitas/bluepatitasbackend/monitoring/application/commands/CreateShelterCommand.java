package com.bluepatitas.bluepatitasbackend.monitoring.application.commands;

/**
 * CreateShelterCommand
 */
public record CreateShelterCommand(
        String name,
        String city,
        String address,
        String administrator,
        String phone,
        String email
) {
}

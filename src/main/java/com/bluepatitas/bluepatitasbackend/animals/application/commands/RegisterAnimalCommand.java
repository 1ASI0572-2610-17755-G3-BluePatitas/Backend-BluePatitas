package com.bluepatitas.bluepatitasbackend.animals.application.commands;

import java.util.UUID;

/**
 * RegisterAnimalCommand
 * <p>
 * CQRS Write-side command that instructs the application to register a new
 * animal profile in the BluePatitas platform.
 * </p>
 *
 * @param name               the animal's given name
 * @param species            the common species name (e.g., "Dog", "Cat")
 * @param breed              the specific breed (e.g., "Labrador Retriever")
 * @param estimatedAgeMonths the animal's estimated age in months
 * @param assignedPerimeterId the UUID of the initial perimeter zone, or null if unassigned
 */
public record RegisterAnimalCommand(
        String name,
        String species,
        String breed,
        Integer estimatedAgeMonths,
        UUID assignedPerimeterId,
        String photoUrl,
        Double weightKg
) {
}

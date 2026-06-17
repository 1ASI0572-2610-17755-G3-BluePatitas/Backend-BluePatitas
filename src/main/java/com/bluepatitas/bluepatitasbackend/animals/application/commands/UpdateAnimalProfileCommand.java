package com.bluepatitas.bluepatitasbackend.animals.application.commands;

import java.util.UUID;

/**
 * UpdateAnimalProfileCommand
 * <p>
 * CQRS Write-side command that instructs the application to update an existing
 * animal's profile details.
 * </p>
 *
 * @param animalId           the UUID of the animal to update
 * @param name               the updated name
 * @param species            the updated species
 * @param breed              the updated breed
 * @param estimatedAgeMonths the updated estimated age in months
 * @param photoUrl           the updated profile photo URL
 */
public record UpdateAnimalProfileCommand(
        UUID animalId,
        String name,
        String species,
        String breed,
        Integer estimatedAgeMonths,
        String photoUrl,
        Double weightKg
) {
}

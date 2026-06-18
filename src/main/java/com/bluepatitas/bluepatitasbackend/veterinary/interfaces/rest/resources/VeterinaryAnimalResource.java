package com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources;

public record VeterinaryAnimalResource(
        String id,
        String name,
        String species,
        String breed,
        String photoUrl,
        String healthCondition,
        Double weightKg,
        String shelterId) {
}

package com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources;

public record VeterinarianAnimalAssignmentResource(
        String id,
        Long veterinarianId,
        String animalId,
        String shelterId,
        Boolean active) {
}

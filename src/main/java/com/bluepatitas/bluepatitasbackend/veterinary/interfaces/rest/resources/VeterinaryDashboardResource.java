package com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources;

public record VeterinaryDashboardResource(
        Long veterinarianId,
        String veterinarianName,
        String shelterId,
        String shelterName,
        Integer assignedAnimalsCount,
        Integer pendingObservationsCount,
        Integer activeAlertsCount,
        Integer recentObservationsCount) {
}

package com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;

import java.util.Optional;
import java.util.UUID;

/**
 * ShelterRepository domain interface.
 */
public interface ShelterRepository {
    Shelter save(Shelter shelter);
    Optional<Shelter> findById(UUID id);
    Optional<Shelter> findFirst();
}

package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.ShelterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * RelationalShelterRepository
 */
@Repository
@RequiredArgsConstructor
public class RelationalShelterRepository implements ShelterRepository {

    private final JpaShelterRepository jpaShelterRepository;

    @Override
    public Shelter save(Shelter shelter) {
        return jpaShelterRepository.save(shelter);
    }

    @Override
    public Optional<Shelter> findById(UUID id) {
        return jpaShelterRepository.findById(id);
    }

    @Override
    public Optional<Shelter> findFirst() {
        return jpaShelterRepository.findFirst();
    }

    @Override
    public Optional<Shelter> findByName(String name) {
        return jpaShelterRepository.findByName(name);
    }
}

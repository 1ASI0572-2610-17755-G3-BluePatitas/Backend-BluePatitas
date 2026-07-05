package com.bluepatitas.bluepatitasbackend.monitoring.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * JpaShelterRepository
 */
public interface JpaShelterRepository extends JpaRepository<Shelter, UUID> {
    @Query(value = "SELECT * FROM monitoring_shelters LIMIT 1", nativeQuery = true)
    Optional<Shelter> findFirst();
    Optional<Shelter> findByName(String name);
}

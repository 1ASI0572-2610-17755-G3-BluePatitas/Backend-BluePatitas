package com.bluepatitas.bluepatitasbackend.veterinary.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates.VeterinaryObservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JpaVeterinaryObservationRepository
 * <p>
 * Spring Data JPA derived-query repository for {@link VeterinaryObservation} entities.
 * This interface is an infrastructure detail and must NOT be used directly
 * outside of {@link SqlVeterinaryObservationRepository}.
 * </p>
 */
public interface JpaVeterinaryObservationRepository extends JpaRepository<VeterinaryObservation, UUID> {

    /**
     * Fetches all veterinary observations for a specific animal.
     *
     * @param animalId the UUID of the animal
     * @return list of observations for that animal, ordered by DB default
     */
    List<VeterinaryObservation> findAllByAnimalId(UUID animalId);

    /**
     * Fetches all veterinary observations recorded by a specific veterinarian.
     *
     * @param veterinarianId the UUID of the veterinarian
     * @return list of observations created by that veterinarian
     */
    List<VeterinaryObservation> findAllByVeterinarianId(UUID veterinarianId);

    /**
     * Checks whether any observation has been recorded for a given animal.
     *
     * @param animalId the UUID of the animal
     * @return true if at least one observation exists for that animal
     */
    boolean existsByAnimalId(UUID animalId);
}

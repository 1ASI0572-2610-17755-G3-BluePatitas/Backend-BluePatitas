package com.bluepatitas.bluepatitasbackend.veterinary.domain.model.repositories;

import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates.VeterinaryObservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * VeterinaryObservationRepository
 * <p>
 * Domain repository interface for {@link VeterinaryObservation} aggregate roots.
 * Defines the persistence contract without coupling the domain layer to any
 * specific storage technology.
 * </p>
 */
public interface VeterinaryObservationRepository {

    /**
     * Persists a new or updated VeterinaryObservation aggregate.
     *
     * @param observation the observation to save
     * @return the saved VeterinaryObservation
     */
    VeterinaryObservation save(VeterinaryObservation observation);

    /**
     * Retrieves a veterinary observation by its unique identifier.
     *
     * @param id the unique identifier of the observation
     * @return an Optional containing the observation if found
     */
    Optional<VeterinaryObservation> findById(UUID id);

    /**
     * Retrieves all veterinary observations for a specific animal,
     * ordered from most recent to oldest.
     *
     * @param animalId the UUID of the animal
     * @return a list of observations for the given animal
     */
    List<VeterinaryObservation> findAllByAnimalId(UUID animalId);

    /**
     * Retrieves all observations recorded by a specific veterinarian.
     *
     * @param veterinarianId the UUID of the veterinarian
     * @return a list of observations created by that veterinarian
     */
    List<VeterinaryObservation> findAllByVeterinarianId(UUID veterinarianId);

    /**
     * Retrieves all veterinary observations in the system.
     *
     * @return a list of all VeterinaryObservation aggregates
     */
    List<VeterinaryObservation> findAll();

    /**
     * Checks whether any observation exists for a given animal.
     *
     * @param animalId the UUID of the animal
     * @return true if at least one observation has been recorded for that animal
     */
    boolean existsByAnimalId(UUID animalId);

    /**
     * Deletes a veterinary observation by its unique identifier.
     *
     * @param id the unique identifier of the observation to delete
     */
    void deleteById(UUID id);
}

package com.bluepatitas.bluepatitasbackend.animals.domain.model.repositories;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * AnimalRepository
 * <p>
 * Domain repository interface for {@link Animal} aggregate roots.
 * Defines the persistence contract for animal management operations
 * without coupling the domain to any specific storage technology.
 * </p>
 */
public interface AnimalRepository {

    /**
     * Persists a new or updated Animal aggregate.
     *
     * @param animal the animal to save
     * @return the saved Animal
     */
    Animal save(Animal animal);

    /**
     * Retrieves an animal by its unique identifier.
     *
     * @param id the unique identifier of the animal
     * @return an Optional containing the animal if found
     */
    Optional<Animal> findById(UUID id);

    /**
     * Retrieves all animals registered in the platform.
     *
     * @return a list of all Animal aggregates
     */
    List<Animal> findAll();

    /**
     * Retrieves all animals assigned to a specific perimeter zone.
     *
     * @param perimeterId the UUID of the perimeter zone
     * @return a list of animals currently assigned to that perimeter
     */
    List<Animal> findAllByAssignedPerimeterId(UUID perimeterId);

    /**
     * Retrieves all animals with a specific health condition.
     *
     * @param status the health condition to filter by
     * @return a list of animals with the given health status
     */
    List<Animal> findAllByHealthCondition(HealthStatus status);

    /**
     * Checks whether an animal with the given name already exists.
     *
     * @param name the name to check
     * @return true if at least one animal with that name is registered
     */
    boolean existsByName(String name);

    /**
     * Deletes an animal by its unique identifier.
     *
     * @param id the unique identifier of the animal to delete
     */
    void deleteById(UUID id);

    /**
     * Retrieves all animals belonging to a specific shelter.
     *
     * @param shelterId the shelter UUID
     * @return a list of Animal aggregates in that shelter
     */
    List<Animal> findAllByShelterId(UUID shelterId);
}

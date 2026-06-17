package com.bluepatitas.bluepatitasbackend.animals.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JpaAnimalRepository
 * <p>
 * Spring Data JPA derived-query repository for {@link Animal} entities.
 * This interface is an infrastructure detail and must NOT be used directly
 * outside of {@link RelationalAnimalRepository}.
 * </p>
 */
public interface JpaAnimalRepository extends JpaRepository<Animal, UUID> {

    /**
     * Fetches all animals assigned to a specific monitoring perimeter zone.
     *
     * @param assignedPerimeterId the perimeter zone UUID
     * @return list of animals in that zone
     */
    List<Animal> findAllByAssignedPerimeterId(UUID assignedPerimeterId);

    /**
     * Fetches all animals that match a given health condition.
     *
     * @param healthCondition the health status to filter by
     * @return list of animals with that health condition
     */
    List<Animal> findAllByHealthCondition(HealthStatus healthCondition);

    /**
     * Checks whether any animal with the given name is registered.
     *
     * @param name the name to check
     * @return true if at least one animal has that name
     */
    boolean existsByName(String name);

    /**
     * Fetches all animals belonging to a specific shelter.
     *
     * @param shelterId the shelter UUID
     * @return list of animals in that shelter
     */
    List<Animal> findAllByShelterId(UUID shelterId);
}

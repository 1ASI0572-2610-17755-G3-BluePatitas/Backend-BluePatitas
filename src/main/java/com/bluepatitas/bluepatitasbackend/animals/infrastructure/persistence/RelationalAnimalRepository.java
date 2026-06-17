package com.bluepatitas.bluepatitasbackend.animals.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.repositories.AnimalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RelationalAnimalRepository
 * <p>
 * Infrastructure implementation of {@link AnimalRepository} backed by a
 * relational database (MySQL) via Spring Data JPA. Provides transactional
 * persistence for the {@link Animal} aggregate root.
 * </p>
 *
 * <p><b>Design notes:</b>
 * <ul>
 *   <li>Uses a dedicated {@link JpaAnimalRepository} Spring Data interface to
 *       keep the domain repository interface free of JPA framework concerns.</li>
 *   <li>An index on {@code (assigned_perimeter_id)} and
 *       {@code (health_condition)} is recommended for efficient filtered
 *       queries at scale.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RelationalAnimalRepository implements AnimalRepository {

    private final JpaAnimalRepository jpaAnimalRepository;

    @Override
    public Animal save(Animal animal) {
        log.debug("Persisting Animal id={}, name='{}'", animal.getId(), animal.getName());
        return jpaAnimalRepository.save(animal);
    }

    @Override
    public Optional<Animal> findById(UUID id) {
        log.debug("Fetching Animal by id={}", id);
        return jpaAnimalRepository.findById(id);
    }

    @Override
    public List<Animal> findAll() {
        log.debug("Fetching all registered animals");
        return jpaAnimalRepository.findAll();
    }

    @Override
    public List<Animal> findAllByAssignedPerimeterId(UUID perimeterId) {
        log.debug("Fetching animals assigned to perimeterId={}", perimeterId);
        return jpaAnimalRepository.findAllByAssignedPerimeterId(perimeterId);
    }

    @Override
    public List<Animal> findAllByHealthCondition(HealthStatus status) {
        log.debug("Fetching animals with healthCondition={}", status);
        return jpaAnimalRepository.findAllByHealthCondition(status);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaAnimalRepository.existsByName(name);
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting Animal id={}", id);
        jpaAnimalRepository.deleteById(id);
    }

    @Override
    public List<Animal> findAllByShelterId(UUID shelterId) {
        log.debug("Fetching animals belonging to shelterId={}", shelterId);
        return jpaAnimalRepository.findAllByShelterId(shelterId);
    }
}

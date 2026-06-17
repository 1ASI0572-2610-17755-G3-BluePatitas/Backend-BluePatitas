package com.bluepatitas.bluepatitasbackend.veterinary.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates.VeterinaryObservation;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.repositories.VeterinaryObservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SqlVeterinaryObservationRepository
 * <p>
 * Infrastructure implementation of {@link VeterinaryObservationRepository}
 * backed by a relational database (MySQL) via Spring Data JPA. Provides
 * transactional persistence for the {@link VeterinaryObservation} aggregate root.
 * </p>
 *
 * <p><b>Design notes:</b>
 * <ul>
 *   <li>Uses a dedicated {@link JpaVeterinaryObservationRepository} Spring Data
 *       interface to keep the domain repository interface free of JPA concerns.</li>
 *   <li>An index on {@code (animal_id)} and {@code (veterinarian_id)} is
 *       recommended for efficient filtered queries.</li>
 * </ul>
 * </p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SqlVeterinaryObservationRepository implements VeterinaryObservationRepository {

    private final JpaVeterinaryObservationRepository jpaObservationRepository;

    @Override
    public VeterinaryObservation save(VeterinaryObservation observation) {
        log.debug("Persisting VeterinaryObservation id={} for animalId={}",
                observation.getId(), observation.getAnimalId());
        return jpaObservationRepository.save(observation);
    }

    @Override
    public Optional<VeterinaryObservation> findById(UUID id) {
        log.debug("Fetching VeterinaryObservation by id={}", id);
        return jpaObservationRepository.findById(id);
    }

    @Override
    public List<VeterinaryObservation> findAllByAnimalId(UUID animalId) {
        log.debug("Fetching veterinary observations for animalId={}", animalId);
        return jpaObservationRepository.findAllByAnimalId(animalId);
    }

    @Override
    public List<VeterinaryObservation> findAllByVeterinarianId(UUID veterinarianId) {
        log.debug("Fetching veterinary observations by veterinarianId={}", veterinarianId);
        return jpaObservationRepository.findAllByVeterinarianId(veterinarianId);
    }

    @Override
    public List<VeterinaryObservation> findAll() {
        log.debug("Fetching all veterinary observations");
        return jpaObservationRepository.findAll();
    }

    @Override
    public boolean existsByAnimalId(UUID animalId) {
        return jpaObservationRepository.existsByAnimalId(animalId);
    }

    @Override
    public void deleteById(UUID id) {
        log.debug("Deleting VeterinaryObservation id={}", id);
        jpaObservationRepository.deleteById(id);
    }
}

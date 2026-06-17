package com.bluepatitas.bluepatitasbackend.animals.application.services;

import com.bluepatitas.bluepatitasbackend.animals.application.commands.AssignPerimeterCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.commands.RegisterAnimalCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.commands.UpdateHealthCommand;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.repositories.AnimalRepository;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.valueobjects.SpeciesInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * AnimalManagementService
 * <p>
 * Application service that orchestrates all write and read operations related
 * to the {@link Animal} aggregate root within the Animals Bounded Context.
 * Acts as the primary entry point for the CQRS command and query handlers.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnimalManagementService {

    private final AnimalRepository animalRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Command Handlers (Write Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new animal in the platform.
     * <p>
     * Constructs the {@link SpeciesInfo} value object and delegates to the
     * {@link Animal} aggregate constructor, persisting the result via the
     * domain repository.
     * </p>
     *
     * @param command the CQRS command carrying all registration data
     * @return the newly persisted Animal aggregate
     */
    @Transactional
    public Animal registerAnimal(RegisterAnimalCommand command) {
        log.info("Registering new animal: name='{}', species='{}', breed='{}'",
                command.name(), command.species(), command.breed());

        SpeciesInfo speciesInfo = new SpeciesInfo(
                command.species(),
                command.breed(),
                command.estimatedAgeMonths()
        );

        Animal animal = new Animal(
                UUID.randomUUID(),
                command.name(),
                speciesInfo,
                HealthStatus.HEALTHY,
                command.assignedPerimeterId()
        );

        Animal saved = animalRepository.save(animal);
        log.info("Animal REGISTERED with id={}", saved.getId());
        return saved;
    }

    /**
     * Updates the health condition of an existing animal.
     * <p>
     * Loads the aggregate, applies the domain behaviour method
     * {@link Animal#registerHealthCondition(HealthStatus)}, and persists.
     * </p>
     *
     * @param command the CQRS command specifying the animal and the new status
     * @return the updated Animal aggregate
     * @throws NoSuchElementException if no animal is found with the given id
     */
    @Transactional
    public Animal updateHealth(UpdateHealthCommand command) {
        log.info("Updating health for animalId={} to status={}", command.animalId(), command.newStatus());

        Animal animal = animalRepository.findById(command.animalId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Animal not found with id: " + command.animalId()));

        animal.registerHealthCondition(command.newStatus());

        Animal updated = animalRepository.save(animal);
        log.info("Health UPDATED for animalId={}, newStatus={}", updated.getId(), updated.getHealthCondition());
        return updated;
    }

    /**
     * Assigns or relocates an animal to a monitoring perimeter zone.
     * <p>
     * Loads the aggregate, applies {@link Animal#relocateToPerimeter(UUID)},
     * and persists the change.
     * </p>
     *
     * @param command the CQRS command specifying the animal and target perimeter
     * @return the updated Animal aggregate with the new perimeter assignment
     * @throws NoSuchElementException if no animal is found with the given id
     */
    @Transactional
    public Animal assignPerimeter(AssignPerimeterCommand command) {
        log.info("Assigning animalId={} to perimeterId={}", command.animalId(), command.perimeterId());

        Animal animal = animalRepository.findById(command.animalId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Animal not found with id: " + command.animalId()));

        animal.relocateToPerimeter(command.perimeterId());

        Animal updated = animalRepository.save(animal);
        log.info("Animal RELOCATED: animalId={}, perimeterId={}", updated.getId(), updated.getAssignedPerimeterId());
        return updated;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query Handlers (Read Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a single animal by its unique identifier.
     *
     * @param id the UUID of the animal to retrieve
     * @return the Animal aggregate
     * @throws NoSuchElementException if no animal is found with the given id
     */
    @Transactional(readOnly = true)
    public Animal getAnimalById(UUID id) {
        log.info("Fetching animal by id={}", id);
        return animalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Animal not found with id: " + id));
    }

    /**
     * Retrieves all registered animals in the platform.
     *
     * @return a list of all Animal aggregates
     */
    @Transactional(readOnly = true)
    public List<Animal> getAllAnimals() {
        log.info("Fetching all registered animals");
        return animalRepository.findAll();
    }
}

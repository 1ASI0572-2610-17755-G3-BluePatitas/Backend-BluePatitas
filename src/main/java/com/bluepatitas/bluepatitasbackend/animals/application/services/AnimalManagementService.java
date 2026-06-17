package com.bluepatitas.bluepatitasbackend.animals.application.services;

import com.bluepatitas.bluepatitasbackend.animals.application.commands.AssignPerimeterCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.commands.RegisterAnimalCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.commands.UpdateHealthCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.commands.UpdateAnimalProfileCommand;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.repositories.AnimalRepository;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.valueobjects.SpeciesInfo;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.aggregates.User;
import com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * AnimalManagementService
 * <p>
 * Application service that orchestrates all write and read operations related
 * to the {@link Animal} aggregate root within the Animals Bounded Context.
 * Supports multi-tenant isolation by restricting operations by user shelterId.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnimalManagementService {

    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    private Optional<UUID> getCurrentUserShelterId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).map(User::getShelterId);
        }
        return Optional.empty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Command Handlers (Write Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new animal in the platform.
     *
     * @param command the CQRS command carrying all registration data
     * @return the newly persisted Animal aggregate
     */
    @Transactional
    public Animal registerAnimal(RegisterAnimalCommand command) {
        log.info("Registering new animal: name='{}', species='{}', breed='{}'",
                command.name(), command.species(), command.breed());

        UUID shelterId = getCurrentUserShelterId()
                .orElseThrow(() -> new IllegalStateException("User is not associated with any shelter."));

        SpeciesInfo speciesInfo = new SpeciesInfo(
                command.species(),
                command.breed(),
                command.estimatedAgeMonths()
        );

        Animal animal = new Animal(
                UUID.randomUUID(),
                shelterId,
                command.name(),
                speciesInfo,
                HealthStatus.HEALTHY,
                command.assignedPerimeterId(),
                command.photoUrl(),
                command.weightKg()
        );

        Animal saved = animalRepository.save(animal);
        log.info("Animal REGISTERED with id={} under shelterId={}", saved.getId(), shelterId);
        return saved;
    }

    /**
     * Updates the health condition of an existing animal.
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

        UUID shelterId = getCurrentUserShelterId()
                .orElseThrow(() -> new IllegalStateException("User is not associated with any shelter."));
        if (!shelterId.equals(animal.getShelterId())) {
            throw new SecurityException("You do not have permission to modify this animal.");
        }

        animal.registerHealthCondition(command.newStatus());

        Animal updated = animalRepository.save(animal);
        log.info("Health UPDATED for animalId={}, newStatus={}", updated.getId(), updated.getHealthCondition());
        return updated;
    }

    /**
     * Assigns or relocates an animal to a monitoring perimeter zone.
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

        UUID shelterId = getCurrentUserShelterId()
                .orElseThrow(() -> new IllegalStateException("User is not associated with any shelter."));
        if (!shelterId.equals(animal.getShelterId())) {
            throw new SecurityException("You do not have permission to modify this animal.");
        }

        animal.relocateToPerimeter(command.perimeterId());

        Animal updated = animalRepository.save(animal);
        log.info("Animal RELOCATED: animalId={}, perimeterId={}", updated.getId(), updated.getAssignedPerimeterId());
        return updated;
    }

    /**
     * Updates the general profile details of an existing animal.
     *
     * @param command the CQRS command carrying updated details
     * @return the updated Animal aggregate
     * @throws NoSuchElementException if no animal is found with the given id
     */
    @Transactional
    public Animal updateAnimalProfile(UpdateAnimalProfileCommand command) {
        log.info("Updating profile details for animalId={}", command.animalId());

        Animal animal = animalRepository.findById(command.animalId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Animal not found with id: " + command.animalId()));

        UUID shelterId = getCurrentUserShelterId()
                .orElseThrow(() -> new IllegalStateException("User is not associated with any shelter."));
        if (!shelterId.equals(animal.getShelterId())) {
            throw new SecurityException("You do not have permission to modify this animal.");
        }

        SpeciesInfo newSpeciesInfo = new SpeciesInfo(
                command.species(),
                command.breed(),
                command.estimatedAgeMonths()
        );

        animal.updateProfile(command.name(), newSpeciesInfo, command.photoUrl(), command.weightKg());

        Animal updated = animalRepository.save(animal);
        log.info("Profile details UPDATED for animalId={}", updated.getId());
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
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Animal not found with id: " + id));

        UUID shelterId = getCurrentUserShelterId()
                .orElseThrow(() -> new IllegalStateException("User is not associated with any shelter."));
        if (!shelterId.equals(animal.getShelterId())) {
            throw new SecurityException("You do not have permission to access this animal.");
        }

        return animal;
    }

    /**
     * Retrieves all registered animals in the platform.
     *
     * @return a list of all Animal aggregates
     */
    @Transactional(readOnly = true)
    public List<Animal> getAllAnimals() {
        log.info("Fetching all registered animals for current user shelter");
        return getCurrentUserShelterId()
                .map(animalRepository::findAllByShelterId)
                .orElse(Collections.emptyList());
    }
}

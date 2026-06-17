package com.bluepatitas.bluepatitasbackend.animals.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.animals.application.commands.AssignPerimeterCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.commands.RegisterAnimalCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.commands.UpdateHealthCommand;
import com.bluepatitas.bluepatitasbackend.animals.application.services.AnimalManagementService;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AnimalProfileController
 * <p>
 * REST interface for animal profile management within the Animals Bounded
 * Context. Exposes endpoints for registering animals, retrieving their
 * profiles, updating health conditions, and assigning perimeter zones.
 * </p>
 */
@RestController
@RequestMapping("/api/animals")
@RequiredArgsConstructor
@Tag(name = "Animals – Profiles", description = "Endpoints for animal registration and profile management")
public class AnimalProfileController {

    private final AnimalManagementService animalManagementService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/animals
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new animal in the BluePatitas platform.
     *
     * @param request the request body containing all registration data
     * @return 201 Created with the persisted Animal aggregate
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new animal",
               description = "Creates and persists a new animal profile. Health condition defaults to HEALTHY.")
    public ResponseEntity<Animal> registerAnimal(@RequestBody RegisterAnimalRequest request) {
        RegisterAnimalCommand command = new RegisterAnimalCommand(
                request.name(),
                request.species(),
                request.breed(),
                request.estimatedAgeMonths(),
                request.assignedPerimeterId()
        );
        Animal created = animalManagementService.registerAnimal(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/animals/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves the full profile of a registered animal by its unique identifier.
     *
     * @param id the UUID of the animal to retrieve
     * @return 200 OK with the Animal aggregate, or 404 if not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get animal by ID",
               description = "Returns the complete profile of a registered animal, including species details and health condition.")
    public ResponseEntity<Animal> getAnimalById(@PathVariable UUID id) {
        Animal animal = animalManagementService.getAnimalById(id);
        return ResponseEntity.ok(animal);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/animals
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all animals registered in the platform.
     *
     * @return 200 OK with the full list of Animal aggregates
     */
    @GetMapping
    @Operation(summary = "List all animals",
               description = "Returns all animal profiles registered in the BluePatitas platform.")
    public ResponseEntity<List<Animal>> getAllAnimals() {
        List<Animal> animals = animalManagementService.getAllAnimals();
        return ResponseEntity.ok(animals);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/animals/{id}/health
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the health condition of an existing animal.
     *
     * @param id      the UUID of the animal
     * @param request the request body containing the new health status
     * @return 200 OK with the updated Animal aggregate
     */
    @PutMapping("/{id}/health")
    @Operation(summary = "Update animal health condition",
               description = "Applies a new health status to an existing animal (HEALTHY, IN_TREATMENT, CRITICAL, UNDER_OBSERVATION).")
    public ResponseEntity<Animal> updateHealth(
            @PathVariable UUID id,
            @RequestBody UpdateHealthRequest request) {

        UpdateHealthCommand command = new UpdateHealthCommand(id, request.healthCondition());
        Animal updated = animalManagementService.updateHealth(command);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/animals/{id}/perimeter
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Assigns or relocates an animal to a monitoring perimeter zone.
     *
     * @param id      the UUID of the animal
     * @param request the request body containing the target perimeter UUID
     * @return 200 OK with the updated Animal aggregate
     */
    @PutMapping("/{id}/perimeter")
    @Operation(summary = "Assign animal to perimeter zone",
               description = "Relocates an animal to a specific monitoring perimeter. Send perimeterId as null to unassign.")
    public ResponseEntity<Animal> assignPerimeter(
            @PathVariable UUID id,
            @RequestBody AssignPerimeterRequest request) {

        AssignPerimeterCommand command = new AssignPerimeterCommand(id, request.perimeterId());
        Animal updated = animalManagementService.assignPerimeter(command);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Request DTOs
    // ─────────────────────────────────────────────────────────────────────────

    /** Request body for POST /api/animals. */
    public record RegisterAnimalRequest(
            String name,
            String species,
            String breed,
            Integer estimatedAgeMonths,
            UUID assignedPerimeterId
    ) {}

    /** Request body for PUT /api/animals/{id}/health. */
    public record UpdateHealthRequest(HealthStatus healthCondition) {}

    /** Request body for PUT /api/animals/{id}/perimeter. */
    public record AssignPerimeterRequest(UUID perimeterId) {}
}

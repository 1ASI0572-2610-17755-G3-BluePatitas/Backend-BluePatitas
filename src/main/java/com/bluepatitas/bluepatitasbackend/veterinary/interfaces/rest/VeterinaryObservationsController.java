package com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.veterinary.application.commands.AddVeterinaryRecommendationCommand;
import com.bluepatitas.bluepatitasbackend.veterinary.application.commands.CreateVeterinaryObservationCommand;
import com.bluepatitas.bluepatitasbackend.veterinary.application.services.VeterinaryObservationCommandHandler;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates.VeterinaryObservation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * VeterinaryObservationsController
 * <p>
 * REST interface for veterinary observation management within the Veterinary
 * Bounded Context. Exposes endpoints for creating clinical observations,
 * adding dietary recommendations, and querying observations by ID or animal.
 * </p>
 */
@RestController
@RequestMapping("/api/veterinary")
@RequiredArgsConstructor
@Tag(name = "Veterinary – Observations", description = "Endpoints for veterinary clinical observation management")
public class VeterinaryObservationsController {

    private final VeterinaryObservationCommandHandler observationCommandHandler;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/veterinary/observations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new veterinary clinical observation for an animal.
     * The observation is created without a recommendation — one can be
     * added via the separate recommendation endpoint.
     *
     * @param request the request body with observation creation data
     * @return 201 Created with the persisted VeterinaryObservation
     */
    @PostMapping("/observations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create veterinary observation",
               description = "Records a new clinical observation for an animal. No recommendation is required at creation time.")
    public ResponseEntity<VeterinaryObservation> createObservation(
            @RequestBody CreateObservationRequest request) {

        CreateVeterinaryObservationCommand command = new CreateVeterinaryObservationCommand(
                request.animalId(),
                request.veterinarianId(),
                request.description()
        );
        VeterinaryObservation created = observationCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/veterinary/observations/{id}/recommendation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Adds a dietary or treatment recommendation to an existing observation.
     * <p>
     * This operation also publishes a domain event that triggers automatic
     * feeding plan creation in the Feeding Bounded Context.
     * </p>
     *
     * @param id      the UUID of the observation to update
     * @param request the request body containing the recommendation text
     * @return 200 OK with the updated VeterinaryObservation
     */
    @PostMapping("/observations/{id}/recommendation")
    @Operation(summary = "Add veterinary recommendation",
               description = "Attaches a dietary/treatment recommendation to an existing observation. Triggers automatic FeedingPlan creation.")
    public ResponseEntity<VeterinaryObservation> addRecommendation(
            @PathVariable UUID id,
            @RequestBody AddRecommendationRequest request) {

        AddVeterinaryRecommendationCommand command = new AddVeterinaryRecommendationCommand(
                id,
                request.recommendation()
        );
        VeterinaryObservation updated = observationCommandHandler.handle(command);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/veterinary/observations/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves a single veterinary observation by its unique identifier.
     *
     * @param id the UUID of the observation
     * @return 200 OK with the VeterinaryObservation, or 404 if not found
     */
    @GetMapping("/observations/{id}")
    @Operation(summary = "Get observation by ID",
               description = "Returns the full details of a veterinary observation, including the recommendation if one has been added.")
    public ResponseEntity<VeterinaryObservation> getObservationById(@PathVariable UUID id) {
        VeterinaryObservation observation = observationCommandHandler.getObservationById(id);
        return ResponseEntity.ok(observation);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/veterinary/animals/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all veterinary observations recorded for a specific animal.
     *
     * @param id the UUID of the animal
     * @return 200 OK with the list of observations for that animal
     */
    @GetMapping("/animals/{id}")
    @Operation(summary = "Get observations by animal",
               description = "Returns all veterinary observations (with or without recommendations) for the specified animal.")
    public ResponseEntity<List<VeterinaryObservation>> getObservationsByAnimalId(@PathVariable UUID id) {
        List<VeterinaryObservation> observations = observationCommandHandler.getObservationsByAnimalId(id);
        return ResponseEntity.ok(observations);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Request DTOs
    // ─────────────────────────────────────────────────────────────────────────

    /** Request body for POST /api/veterinary/observations. */
    public record CreateObservationRequest(
            UUID animalId,
            Long veterinarianId,
            String description
    ) {}

    /** Request body for POST /api/veterinary/observations/{id}/recommendation. */
    public record AddRecommendationRequest(String recommendation) {}
}

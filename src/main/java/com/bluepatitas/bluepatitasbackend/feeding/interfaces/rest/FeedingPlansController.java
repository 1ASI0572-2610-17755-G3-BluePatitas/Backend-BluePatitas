package com.bluepatitas.bluepatitasbackend.feeding.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.feeding.application.commands.CreateFeedingPlanCommand;
import com.bluepatitas.bluepatitasbackend.feeding.application.commands.UpdateFeedingPlanCommand;
import com.bluepatitas.bluepatitasbackend.feeding.application.services.FeedingPlanCommandHandler;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates.FeedingPlan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * FeedingPlansController
 * <p>
 * REST interface for feeding plan management within the Feeding Bounded Context.
 * Exposes endpoints for creating plans, retrieving plans by animal, updating
 * plan configuration, and managing the plan lifecycle (activate / deactivate).
 * </p>
 */
@RestController
@RequestMapping("/api/feeding/plans")
@RequiredArgsConstructor
@Tag(name = "Feeding – Plans", description = "Endpoints for animal feeding plan management")
public class FeedingPlansController {

    private final FeedingPlanCommandHandler feedingPlanCommandHandler;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/feeding/plans
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new feeding plan for an animal in DRAFT state.
     *
     * @param request the request body with all plan creation parameters
     * @return 201 Created with the persisted FeedingPlan
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create feeding plan",
               description = "Creates a new feeding plan in DRAFT state. Must be activated before dispenser events are triggered.")
    public ResponseEntity<FeedingPlan> createFeedingPlan(@RequestBody CreatePlanRequest request) {
        CreateFeedingPlanCommand command = new CreateFeedingPlanCommand(
                request.animalId(),
                request.dietName(),
                request.nutritionalNotes(),
                request.foodQuantity(),
                request.foodUnit(),
                request.timesPerDay(),
                request.scheduledTimes(),
                request.toleranceMinutes()
        );
        FeedingPlan created = feedingPlanCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/feeding/plans
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all feeding plans.
     *
     * @return 200 OK with the list of all feeding plans
     */
    @GetMapping
    @Operation(summary = "Get all plans",
               description = "Returns all feeding plans registered in the shelter.")
    public ResponseEntity<List<FeedingPlan>> getAllPlans() {
        List<FeedingPlan> plans = feedingPlanCommandHandler.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/feeding/plans/{animalId}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all feeding plans associated with a specific animal.
     *
     * @param animalId the UUID of the animal
     * @return 200 OK with the list of feeding plans for that animal
     */
    @GetMapping("/{animalId}")
    @Operation(summary = "Get plans by animal",
               description = "Returns all feeding plans (regardless of status) for the specified animal.")
    public ResponseEntity<List<FeedingPlan>> getPlansByAnimalId(@PathVariable UUID animalId) {
        List<FeedingPlan> plans = feedingPlanCommandHandler.getPlansByAnimalId(animalId);
        return ResponseEntity.ok(plans);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/feeding/plans/{id}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Partially updates an existing feeding plan.
     * Only non-null fields in the request body are applied.
     *
     * @param id      the UUID of the feeding plan to update
     * @param request the request body with updated field values
     * @return 200 OK with the updated FeedingPlan
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update feeding plan",
               description = "Partially updates a feeding plan. Null fields are ignored and keep their current values.")
    public ResponseEntity<FeedingPlan> updateFeedingPlan(
            @PathVariable UUID id,
            @RequestBody UpdatePlanRequest request) {

        UpdateFeedingPlanCommand command = new UpdateFeedingPlanCommand(
                id,
                request.dietName(),
                request.nutritionalNotes(),
                request.foodQuantity(),
                request.foodUnit(),
                request.timesPerDay(),
                request.scheduledTimes(),
                request.toleranceMinutes()
        );
        FeedingPlan updated = feedingPlanCommandHandler.handle(command);
        return ResponseEntity.ok(updated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/feeding/plans/{id}/activate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Activates a feeding plan, enabling dispenser device execution.
     *
     * @param id the UUID of the feeding plan to activate
     * @return 200 OK with the activated FeedingPlan
     */
    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate feeding plan",
               description = "Transitions the plan from DRAFT or INACTIVE to ACTIVE. The dispenser will start executing feedings.")
    public ResponseEntity<FeedingPlan> activatePlan(@PathVariable UUID id) {
        FeedingPlan activated = feedingPlanCommandHandler.activate(id);
        return ResponseEntity.ok(activated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/feeding/plans/{id}/deactivate
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Deactivates a feeding plan, pausing all dispenser executions.
     * Plan data is preserved and the plan can be reactivated later.
     *
     * @param id the UUID of the feeding plan to deactivate
     * @return 200 OK with the deactivated FeedingPlan
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate feeding plan",
               description = "Pauses the feeding plan. Feedings stop but the plan data is preserved for future reactivation.")
    public ResponseEntity<FeedingPlan> deactivatePlan(@PathVariable UUID id) {
        FeedingPlan deactivated = feedingPlanCommandHandler.deactivate(id);
        return ResponseEntity.ok(deactivated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner Request DTOs
    // ─────────────────────────────────────────────────────────────────────────

    /** Request body for POST /api/feeding/plans. */
    public record CreatePlanRequest(
            UUID animalId,
            String dietName,
            String nutritionalNotes,
            BigDecimal foodQuantity,
            String foodUnit,
            Integer timesPerDay,
            String scheduledTimes,
            Integer toleranceMinutes
    ) {}

    /** Request body for PUT /api/feeding/plans/{id}. */
    public record UpdatePlanRequest(
            String dietName,
            String nutritionalNotes,
            BigDecimal foodQuantity,
            String foodUnit,
            Integer timesPerDay,
            String scheduledTimes,
            Integer toleranceMinutes
    ) {}
}

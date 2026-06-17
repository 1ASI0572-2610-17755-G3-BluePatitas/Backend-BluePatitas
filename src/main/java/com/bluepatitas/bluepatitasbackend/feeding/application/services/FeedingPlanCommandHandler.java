package com.bluepatitas.bluepatitasbackend.feeding.application.services;

import com.bluepatitas.bluepatitasbackend.feeding.application.commands.CreateFeedingPlanCommand;
import com.bluepatitas.bluepatitasbackend.feeding.application.commands.UpdateFeedingPlanCommand;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates.FeedingPlan;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.repositories.FeedingPlanRepository;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.DietType;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.FeedingSchedule;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.FoodAmount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * FeedingPlanCommandHandler
 * <p>
 * Application service that processes CQRS write-side commands for the
 * {@link FeedingPlan} aggregate. Responsible for:
 * <ul>
 *   <li>Creating new feeding plans from {@link CreateFeedingPlanCommand}</li>
 *   <li>Applying partial updates via {@link UpdateFeedingPlanCommand}</li>
 *   <li>Handling lifecycle transitions (activate / deactivate)</li>
 *   <li>Providing read-side query operations</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedingPlanCommandHandler {

    private final FeedingPlanRepository feedingPlanRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Command Handlers (Write Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a new {@link FeedingPlan} in DRAFT state.
     * <p>
     * Assembles the three value objects ({@link DietType}, {@link FoodAmount},
     * {@link FeedingSchedule}) from the flat command fields and delegates
     * creation to the aggregate's factory method.
     * </p>
     *
     * @param command the CQRS command carrying all plan creation data
     * @return the newly persisted FeedingPlan in DRAFT state
     */
    @Transactional
    public FeedingPlan handle(CreateFeedingPlanCommand command) {
        log.info("Creating FeedingPlan for animalId={}, diet='{}', schedule='{}'",
                command.animalId(), command.dietName(), command.scheduledTimes());

        DietType dietType = new DietType(command.dietName(), command.nutritionalNotes());
        FoodAmount foodAmount = new FoodAmount(command.foodQuantity(), command.foodUnit());
        FeedingSchedule schedule = new FeedingSchedule(
                command.timesPerDay(),
                command.scheduledTimes(),
                command.toleranceMinutes()
        );

        FeedingPlan plan = FeedingPlan.create(
                UUID.randomUUID(),
                command.animalId(),
                dietType,
                foodAmount,
                schedule
        );

        FeedingPlan saved = feedingPlanRepository.save(plan);
        log.info("FeedingPlan CREATED with id={} for animalId={}", saved.getId(), saved.getAnimalId());
        return saved;
    }

    /**
     * Applies a partial update to an existing {@link FeedingPlan}.
     * <p>
     * Only non-null fields in the command are applied. Each independent update
     * (diet, food amount, schedule) calls the corresponding domain method,
     * which internally updates {@code updatedAt}.
     * </p>
     *
     * @param command the CQRS command with updated field values (null = no change)
     * @return the updated and persisted FeedingPlan
     * @throws NoSuchElementException if no plan is found with the given planId
     */
    @Transactional
    public FeedingPlan handle(UpdateFeedingPlanCommand command) {
        log.info("Updating FeedingPlan id={}", command.planId());

        FeedingPlan plan = feedingPlanRepository.findById(command.planId())
                .orElseThrow(() -> new NoSuchElementException(
                        "FeedingPlan not found with id: " + command.planId()));

        // Apply diet update if provided
        if (command.dietName() != null) {
            DietType newDiet = new DietType(command.dietName(), command.nutritionalNotes());
            plan.updateDiet(newDiet);
            log.debug("Diet updated for planId={}", plan.getId());
        }

        // Apply food amount update if provided
        if (command.foodQuantity() != null && command.foodUnit() != null) {
            FoodAmount newAmount = new FoodAmount(command.foodQuantity(), command.foodUnit());
            plan.updateFoodAmount(newAmount);
            log.debug("FoodAmount updated for planId={}", plan.getId());
        }

        // Apply schedule update if provided
        if (command.timesPerDay() != null && command.scheduledTimes() != null) {
            FeedingSchedule newSchedule = new FeedingSchedule(
                    command.timesPerDay(),
                    command.scheduledTimes(),
                    command.toleranceMinutes()
            );
            plan.updateSchedule(newSchedule);
            log.debug("Schedule updated for planId={}", plan.getId());
        }

        FeedingPlan updated = feedingPlanRepository.save(plan);
        log.info("FeedingPlan UPDATED id={}", updated.getId());
        return updated;
    }

    /**
     * Activates a feeding plan, enabling dispenser execution.
     *
     * @param planId the UUID of the plan to activate
     * @return the activated FeedingPlan
     * @throws NoSuchElementException if no plan is found with the given planId
     */
    @Transactional
    public FeedingPlan activate(UUID planId) {
        log.info("Activating FeedingPlan id={}", planId);

        FeedingPlan plan = feedingPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException(
                        "FeedingPlan not found with id: " + planId));

        plan.activate();

        FeedingPlan activated = feedingPlanRepository.save(plan);
        log.info("FeedingPlan ACTIVATED id={}", activated.getId());
        return activated;
    }

    /**
     * Deactivates a feeding plan, pausing all dispenser executions.
     *
     * @param planId the UUID of the plan to deactivate
     * @return the deactivated FeedingPlan
     * @throws NoSuchElementException if no plan is found with the given planId
     */
    @Transactional
    public FeedingPlan deactivate(UUID planId) {
        log.info("Deactivating FeedingPlan id={}", planId);

        FeedingPlan plan = feedingPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException(
                        "FeedingPlan not found with id: " + planId));

        plan.deactivate();

        FeedingPlan deactivated = feedingPlanRepository.save(plan);
        log.info("FeedingPlan DEACTIVATED id={}", deactivated.getId());
        return deactivated;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query Handlers (Read Side)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retrieves all feeding plans associated with a specific animal.
     *
     * @param animalId the UUID of the animal
     * @return a list of feeding plans for the given animal
     */
    @Transactional(readOnly = true)
    public List<FeedingPlan> getPlansByAnimalId(UUID animalId) {
        log.info("Fetching feeding plans for animalId={}", animalId);
        return feedingPlanRepository.findAllByAnimalId(animalId);
    }

    /**
     * Retrieves all feeding plans registered in the platform.
     *
     * @return a list of all feeding plans
     */
    @Transactional(readOnly = true)
    public List<FeedingPlan> getAllPlans() {
        log.info("Fetching all feeding plans.");
        return feedingPlanRepository.findAll();
    }

    /**
     * Retrieves a single feeding plan by its unique identifier.
     *
     * @param planId the UUID of the feeding plan
     * @return the FeedingPlan aggregate
     * @throws NoSuchElementException if no plan is found
     */
    @Transactional(readOnly = true)
    public FeedingPlan getPlanById(UUID planId) {
        log.info("Fetching FeedingPlan by id={}", planId);
        return feedingPlanRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException(
                        "FeedingPlan not found with id: " + planId));
    }
}

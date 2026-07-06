package com.bluepatitas.bluepatitasbackend.iam.application.internal.eventhandlers;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.enumerations.HealthStatus;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.repositories.AnimalRepository;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.valueobjects.SpeciesInfo;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.aggregates.FeedingPlan;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.repositories.FeedingPlanRepository;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.DietType;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.FeedingSchedule;
import com.bluepatitas.bluepatitasbackend.feeding.domain.model.valueobjects.FoodAmount;
import com.bluepatitas.bluepatitasbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.aggregates.User;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.Role;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.valueobjects.RoleType;
import com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.MonitoringZone;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.MonitoringZoneRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.ShelterRepository;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates.VeterinaryObservation;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.repositories.VeterinaryObservationRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class DemoDataSeedEventHandler {
    private static final String DEMO_SHELTER_NAME = "WUF Shelter";
    private static final String ADMIN_EMAIL = "admin@bluepatitas.com";
    private static final String VET_EMAIL = "vet@bluepatitas.com";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ShelterRepository shelterRepository;
    private final AnimalRepository animalRepository;
    private final MonitoringZoneRepository monitoringZoneRepository;
    private final FeedingPlanRepository feedingPlanRepository;
    private final VeterinaryObservationRepository veterinaryObservationRepository;
    private final HashingService hashingService;
    private final JdbcTemplate jdbcTemplate;

    public DemoDataSeedEventHandler(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ShelterRepository shelterRepository,
            AnimalRepository animalRepository,
            MonitoringZoneRepository monitoringZoneRepository,
            FeedingPlanRepository feedingPlanRepository,
            VeterinaryObservationRepository veterinaryObservationRepository,
            HashingService hashingService,
            JdbcTemplate jdbcTemplate) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.shelterRepository = shelterRepository;
        this.animalRepository = animalRepository;
        this.monitoringZoneRepository = monitoringZoneRepository;
        this.feedingPlanRepository = feedingPlanRepository;
        this.veterinaryObservationRepository = veterinaryObservationRepository;
        this.hashingService = hashingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener
    @Transactional
    public void on(ApplicationReadyEvent event) {
        Role adminRole = ensureRole(RoleType.ROLE_SHELTER_ADMIN);
        Role veterinarianRole = ensureRole(RoleType.ROLE_VETERINARIAN);
        Shelter shelter = ensureDemoShelter();

        ensureUser(ADMIN_EMAIL, "Carlos", "Admin", "900000001", "admin123", adminRole, shelter.getId());
        User veterinarian = ensureUser(VET_EMAIL, "Elena", "Ramos", "900000002", "vet123", veterinarianRole, shelter.getId());

        List<Animal> animals = ensureDemoAnimals(shelter.getId());
        ensureDemoMonitoringZone(shelter.getId(), animals);
        ensureDemoFeedingPlan(animals);
        normalizeLegacyVeterinaryObservationVeterinarianIds(veterinarian);
        ensureDemoVeterinaryObservations(animals, veterinarian);
    }

    private Role ensureRole(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseGet(() -> roleRepository.save(new Role(roleType)));
    }

    private Shelter ensureDemoShelter() {
        return shelterRepository.findByName(DEMO_SHELTER_NAME)
                .orElseGet(() -> shelterRepository.save(new Shelter(
                        UUID.randomUUID(),
                        DEMO_SHELTER_NAME,
                        "Lima",
                        "Av. Demo 123",
                        "Carlos Admin",
                        "900000000",
                        "contacto@bluepatitas.com"
                )));
    }

    private User ensureUser(String email, String firstName, String lastName, String phoneNumber, String password, Role role, UUID shelterId) {
        return userRepository.findByEmail(email).map(user -> {
            if (user.getShelterId() == null) {
                user.setShelterId(shelterId);
                return userRepository.save(user);
            }
            return user;
        }).orElseGet(() -> {
            User user = new User(firstName, lastName, email, phoneNumber, hashingService.encode(password), List.of(role));
            user.setShelterId(shelterId);
            return userRepository.save(user);
        });
    }

    private List<Animal> ensureDemoAnimals(UUID shelterId) {
        List<Animal> existing = animalRepository.findAllByShelterId(shelterId);
        ensureAnimal(existing, shelterId, "Luna", "Dog", "Mixed", 24, 12.5);
        ensureAnimal(existing, shelterId, "Max", "Dog", "Labrador", 36, 21.0);
        ensureAnimal(existing, shelterId, "Michi", "Cat", "Domestic Shorthair", 18, 4.2);
        return animalRepository.findAllByShelterId(shelterId);
    }

    private void ensureAnimal(List<Animal> existing, UUID shelterId, String name, String species, String breed, int ageMonths, double weightKg) {
        boolean alreadyExists = existing.stream().anyMatch(animal -> name.equalsIgnoreCase(animal.getName()));
        if (alreadyExists) {
            return;
        }
        animalRepository.save(new Animal(
                UUID.randomUUID(),
                shelterId,
                name,
                new SpeciesInfo(species, breed, ageMonths),
                HealthStatus.HEALTHY,
                null,
                "/uploads/demo-" + name.toLowerCase() + ".jpg",
                weightKg
        ));
    }

    private void ensureDemoMonitoringZone(UUID shelterId, List<Animal> animals) {
        boolean exists = monitoringZoneRepository.findAllByShelterId(shelterId).stream()
                .anyMatch(zone -> "Patio principal".equalsIgnoreCase(zone.getName()));
        if (exists) {
            return;
        }
        UUID targetId = animals.isEmpty() ? UUID.randomUUID() : animals.get(0).getId();
        monitoringZoneRepository.save(new MonitoringZone(
                UUID.randomUUID(),
                shelterId,
                targetId,
                "Patio principal",
                24.0,
                55.0,
                "Active",
                animals.size(),
                true,
                "/uploads/demo-zone.jpg",
                10.0,
                35.0
        ));
    }

    private void ensureDemoFeedingPlan(List<Animal> animals) {
        if (animals.isEmpty()) {
            return;
        }
        Animal animal = animals.get(0);
        if (!feedingPlanRepository.findAllByAnimalId(animal.getId()).isEmpty()) {
            return;
        }
        FeedingPlan plan = FeedingPlan.create(
                UUID.randomUUID(),
                animal.getId(),
                new DietType("Balanced adult diet", "Demo plan for Web/Mobile testing"),
                new FoodAmount(new BigDecimal("200"), "grams"),
                new FeedingSchedule(2, "08:00,18:00", 10)
        );
        feedingPlanRepository.save(plan);
    }

    private void ensureDemoVeterinaryObservations(List<Animal> animals, User veterinarian) {
        if (animals.isEmpty()) {
            return;
        }
        Animal animal = animals.get(0);
        if (veterinaryObservationRepository.existsByAnimalId(animal.getId())) {
            return;
        }
        VeterinaryObservation observation = VeterinaryObservation.create(
                UUID.randomUUID(),
                animal.getId(),
                veterinarian.getId(),
                "Demo observation: animal is stable and eating normally."
        );
        observation.addRecommendation("Maintain balanced diet and observe hydration.");
        veterinaryObservationRepository.save(observation);
    }

    private void normalizeLegacyVeterinaryObservationVeterinarianIds(User veterinarian) {
        if (isVeterinaryObservationVeterinarianIdBigint()) {
            return;
        }

        if (!columnExists("veterinary_observations", "veterinarian_user_id")) {
            jdbcTemplate.execute("ALTER TABLE veterinary_observations ADD COLUMN veterinarian_user_id BIGINT NULL");
        }
        jdbcTemplate.update("""
                UPDATE veterinary_observations
                SET veterinarian_user_id = ?
                WHERE veterinarian_user_id IS NULL
                """, veterinarian.getId());
        jdbcTemplate.execute("ALTER TABLE veterinary_observations DROP COLUMN veterinarian_id");
        jdbcTemplate.execute("""
                ALTER TABLE veterinary_observations
                CHANGE COLUMN veterinarian_user_id veterinarian_id BIGINT NOT NULL
                """);
    }

    private boolean isVeterinaryObservationVeterinarianIdBigint() {
        String dataType = jdbcTemplate.queryForObject("""
                SELECT DATA_TYPE
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'veterinary_observations'
                  AND COLUMN_NAME = 'veterinarian_id'
                """, String.class);
        return "bigint".equalsIgnoreCase(dataType);
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }
}

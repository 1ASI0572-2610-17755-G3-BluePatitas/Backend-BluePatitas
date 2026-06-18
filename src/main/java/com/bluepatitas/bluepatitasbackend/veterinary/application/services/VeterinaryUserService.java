package com.bluepatitas.bluepatitasbackend.veterinary.application.services;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.repositories.AnimalRepository;
import com.bluepatitas.bluepatitasbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.aggregates.User;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.Role;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.valueobjects.RoleType;
import com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.PerimeterAlert;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.AlertRepository;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.ShelterRepository;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.aggregates.VeterinaryObservation;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.repositories.VeterinaryObservationRepository;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.InvitedVeterinarianResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinarianResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinaryAnimalResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinaryDashboardResource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeterinaryUserService {
    private static final String DEMO_SHELTER_NAME = "WUF Shelter";
    private static final String INVITATION_CODE = "VET-BP-2026";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShelterRepository shelterRepository;
    private final AnimalRepository animalRepository;
    private final VeterinaryObservationRepository veterinaryObservationRepository;
    private final AlertRepository alertRepository;
    private final HashingService hashingService;

    @Transactional(readOnly = true)
    public List<VeterinarianResource> getVeterinarians() {
        UUID currentShelterId = currentUser().map(User::getShelterId).orElse(null);
        return userRepository.findAll().stream()
                .filter(user -> hasRole(user, RoleType.ROLE_VETERINARIAN))
                .filter(user -> currentShelterId == null || currentShelterId.equals(user.getShelterId()))
                .map(this::toVeterinarianResource)
                .toList();
    }

    @Transactional
    public InvitedVeterinarianResource inviteVeterinarian(String email, String firstName, String lastName, String password) {
        validateInvite(email, firstName, lastName, password);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }

        Role veterinarianRole = roleRepository.findByName(RoleType.ROLE_VETERINARIAN)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_VETERINARIAN)));
        Shelter shelter = resolveShelterForInvite();

        User veterinarian = new User(
                firstName.trim(),
                lastName.trim(),
                email.trim().toLowerCase(),
                "000000000",
                hashingService.encode(password),
                List.of(veterinarianRole)
        );
        veterinarian.setShelterId(shelter.getId());
        User saved = userRepository.save(veterinarian);

        return new InvitedVeterinarianResource(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                "VETERINARIAN",
                saved.getShelterId().toString(),
                shelter.getName(),
                "ACTIVE",
                INVITATION_CODE
        );
    }

    @Transactional(readOnly = true)
    public VeterinaryDashboardResource getMyDashboard() {
        User veterinarian = currentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));
        Shelter shelter = resolveShelter(veterinarian.getShelterId());
        List<Animal> animals = animalsForShelter(veterinarian.getShelterId());
        Set<UUID> animalIds = animals.stream().map(Animal::getId).collect(Collectors.toSet());
        List<VeterinaryObservation> observations = veterinaryObservationRepository.findAll().stream()
                .filter(observation -> animalIds.contains(observation.getAnimalId()))
                .toList();
        int activeAlerts = (int) alertRepository.findAll().stream()
                .filter(alert -> animalIds.contains(alert.getTargetId()))
                .filter(this::isActiveAlert)
                .count();
        int pendingObservations = (int) observations.stream()
                .filter(observation -> observation.getRecommendation() == null || observation.getRecommendation().isBlank())
                .count();

        return new VeterinaryDashboardResource(
                veterinarian.getId(),
                veterinarian.getFirstName() + " " + veterinarian.getLastName(),
                veterinarian.getShelterId() != null ? veterinarian.getShelterId().toString() : null,
                shelter != null ? shelter.getName() : null,
                animals.size(),
                pendingObservations,
                activeAlerts,
                observations.size()
        );
    }

    @Transactional(readOnly = true)
    public List<VeterinaryAnimalResource> getMyAnimals() {
        User veterinarian = currentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));
        return animalsForShelter(veterinarian.getShelterId()).stream()
                .map(this::toVeterinaryAnimalResource)
                .toList();
    }

    private VeterinarianResource toVeterinarianResource(User user) {
        Shelter shelter = resolveShelter(user.getShelterId());
        int assignedAnimalsCount = animalsForShelter(user.getShelterId()).size();
        return new VeterinarianResource(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                "VETERINARIAN",
                user.getShelterId() != null ? user.getShelterId().toString() : null,
                shelter != null ? shelter.getName() : null,
                "ACTIVE",
                assignedAnimalsCount
        );
    }

    private VeterinaryAnimalResource toVeterinaryAnimalResource(Animal animal) {
        return new VeterinaryAnimalResource(
                animal.getId().toString(),
                animal.getName(),
                animal.getSpeciesDetails().species(),
                animal.getSpeciesDetails().breed(),
                animal.getPhotoUrl(),
                animal.getHealthCondition().name(),
                animal.getWeightKg(),
                animal.getShelterId().toString()
        );
    }

    private List<Animal> animalsForShelter(UUID shelterId) {
        if (shelterId == null) {
            return List.of();
        }
        return animalRepository.findAllByShelterId(shelterId);
    }

    private Shelter resolveShelter(UUID shelterId) {
        if (shelterId == null) {
            return null;
        }
        return shelterRepository.findById(shelterId).orElse(null);
    }

    private Shelter resolveShelterForInvite() {
        return currentUser()
                .map(User::getShelterId)
                .flatMap(shelterRepository::findById)
                .orElseGet(() -> shelterRepository.findByName(DEMO_SHELTER_NAME)
                        .orElseGet(() -> shelterRepository.save(new Shelter(
                                UUID.randomUUID(),
                                DEMO_SHELTER_NAME,
                                "Lima",
                                "Av. Demo 123",
                                "Carlos Admin",
                                "900000000",
                                "contacto@bluepatitas.com"
                        ))));
    }

    private java.util.Optional<User> currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Optional.empty();
        }
        return userRepository.findByEmail(authentication.getName());
    }

    private boolean hasRole(User user, RoleType roleType) {
        return user.getRoles().stream().anyMatch(role -> roleType.equals(role.getName()));
    }

    private boolean isActiveAlert(PerimeterAlert alert) {
        return Boolean.TRUE.equals(alert.getTrackingActive()) || Boolean.TRUE.equals(alert.getIsBreachConfirmed());
    }

    private void validateInvite(String email, String firstName, String lastName, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
    }
}

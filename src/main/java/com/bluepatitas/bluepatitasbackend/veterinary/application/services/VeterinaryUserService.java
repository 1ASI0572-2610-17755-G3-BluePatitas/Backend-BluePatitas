package com.bluepatitas.bluepatitasbackend.veterinary.application.services;

import com.bluepatitas.bluepatitasbackend.animals.domain.model.aggregates.Animal;
import com.bluepatitas.bluepatitasbackend.animals.domain.model.repositories.AnimalRepository;
import com.bluepatitas.bluepatitasbackend.iam.application.internal.outboundservices.hashing.HashingService;
import com.bluepatitas.bluepatitasbackend.iam.application.internal.outboundservices.tokens.TokenService;
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
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.entities.VeterinarianAnimalAssignment;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.entities.VeterinaryInvitation;
import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.repositories.VeterinaryObservationRepository;
import com.bluepatitas.bluepatitasbackend.veterinary.infrastructure.persistence.JpaVeterinarianAnimalAssignmentRepository;
import com.bluepatitas.bluepatitasbackend.veterinary.infrastructure.persistence.JpaVeterinaryInvitationRepository;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.InvitedVeterinarianResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinarianAnimalAssignmentResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinarianResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinaryAnimalResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinaryDashboardResource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VeterinaryUserService {
    private static final String DEMO_SHELTER_NAME = "WUF Shelter";
    private static final String INVITATION_CODE_PREFIX = "VET-";
    private static final String INVITATION_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITATION_CODE_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ShelterRepository shelterRepository;
    private final AnimalRepository animalRepository;
    private final VeterinaryObservationRepository veterinaryObservationRepository;
    private final AlertRepository alertRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final JpaVeterinaryInvitationRepository veterinaryInvitationRepository;
    private final JpaVeterinarianAnimalAssignmentRepository assignmentRepository;

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
        validateInvite(email, firstName, lastName);
        String normalizedEmail = email.trim().toLowerCase();
        Shelter shelter = resolveShelterForInvite();

        var existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!hasRole(user, RoleType.ROLE_VETERINARIAN) || !shelter.getId().equals(user.getShelterId())) {
                throw new IllegalArgumentException("A user with this email already exists.");
            }
            return veterinaryInvitationRepository.findFirstByVeterinarianUserIdAndStatusIgnoreCase(user.getId(), "PENDING")
                    .map(invitation -> toInvitedVeterinarianResource(user, shelter, invitation))
                    .orElseThrow(() -> new IllegalArgumentException("An active veterinarian with this email already exists."));
        }

        Role veterinarianRole = roleRepository.findByName(RoleType.ROLE_VETERINARIAN)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_VETERINARIAN)));

        User veterinarian = new User(
                firstName.trim(),
                lastName.trim(),
                normalizedEmail,
                "000000000",
                hashingService.encode(generateTemporaryPassword()),
                List.of(veterinarianRole)
        );
        veterinarian.setShelterId(shelter.getId());
        User saved = userRepository.save(veterinarian);
        VeterinaryInvitation invitation = veterinaryInvitationRepository.save(new VeterinaryInvitation(
                UUID.randomUUID(),
                saved.getId(),
                shelter.getId(),
                generateUniqueInvitationCode()
        ));

        return toInvitedVeterinarianResource(saved, shelter, invitation);
    }

    @Transactional
    public AuthenticatedUserResource redeemInvitationCode(String code, String password, String confirmPassword) {
        validateRedeem(code, password, confirmPassword);
        VeterinaryInvitation invitation = veterinaryInvitationRepository.findByInvitationCode(code.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invitation code is invalid."));
        if (!invitation.isPending()) {
            throw new IllegalArgumentException("Invitation code has already been used.");
        }
        User veterinarian = userRepository.findById(invitation.getVeterinarianUserId())
                .orElseThrow(() -> new NoSuchElementException("Veterinarian not found."));
        if (!hasRole(veterinarian, RoleType.ROLE_VETERINARIAN)) {
            throw new IllegalArgumentException("Invitation code does not belong to a veterinarian.");
        }

        veterinarian.setPassword(hashingService.encode(password));
        User saved = userRepository.save(veterinarian);
        invitation.markUsed();
        veterinaryInvitationRepository.save(invitation);

        String token = tokenService.generateToken(saved.getEmail());
        return AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(saved, token);
    }

    @Transactional(readOnly = true)
    public VeterinaryDashboardResource getMyDashboard() {
        User veterinarian = currentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));
        Shelter shelter = resolveShelter(veterinarian.getShelterId());
        List<Animal> animals = animalsAssignedToVeterinarian(veterinarian);
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
        return animalsAssignedToVeterinarian(veterinarian).stream()
                .map(this::toVeterinaryAnimalResource)
                .toList();
    }

    @Transactional
    public VeterinarianAnimalAssignmentResource assignAnimalToVeterinarian(Long veterinarianId, UUID animalId) {
        Shelter adminShelter = resolveShelterForInvite();
        User veterinarian = resolveVeterinarianForAdmin(veterinarianId, adminShelter.getId());
        Animal animal = resolveAnimalForAdmin(animalId, adminShelter.getId());

        var activeAssignment = assignmentRepository.findByVeterinarianUserIdAndAnimalIdAndActiveTrue(veterinarian.getId(), animal.getId());
        if (activeAssignment.isPresent()) {
            return toAssignmentResource(activeAssignment.get());
        }

        VeterinarianAnimalAssignment assignment = assignmentRepository
                .findFirstByVeterinarianUserIdAndAnimalId(veterinarian.getId(), animal.getId())
                .map(existing -> {
                    existing.reactivate();
                    return existing;
                })
                .orElseGet(() -> new VeterinarianAnimalAssignment(
                        UUID.randomUUID(),
                        veterinarian.getId(),
                        animal.getId(),
                        adminShelter.getId()
                ));
        return toAssignmentResource(assignmentRepository.save(assignment));
    }

    @Transactional
    public void unassignAnimalFromVeterinarian(Long veterinarianId, UUID animalId) {
        Shelter adminShelter = resolveShelterForInvite();
        User veterinarian = resolveVeterinarianForAdmin(veterinarianId, adminShelter.getId());
        resolveAnimalForAdmin(animalId, adminShelter.getId());
        assignmentRepository.findByVeterinarianUserIdAndAnimalIdAndActiveTrue(veterinarian.getId(), animalId)
                .ifPresent(assignment -> {
                    assignment.deactivate();
                    assignmentRepository.save(assignment);
                });
    }

    @Transactional(readOnly = true)
    public List<VeterinaryAnimalResource> getAssignedAnimals(Long veterinarianId) {
        Shelter adminShelter = resolveShelterForInvite();
        User veterinarian = resolveVeterinarianForAdmin(veterinarianId, adminShelter.getId());
        return animalsAssignedToVeterinarian(veterinarian).stream()
                .map(this::toVeterinaryAnimalResource)
                .toList();
    }

    private VeterinarianResource toVeterinarianResource(User user) {
        Shelter shelter = resolveShelter(user.getShelterId());
        int assignedAnimalsCount = (int) assignmentRepository.countByVeterinarianUserIdAndActiveTrue(user.getId());
        return new VeterinarianResource(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                RoleType.ROLE_VETERINARIAN.name(),
                user.getShelterId() != null ? user.getShelterId().toString() : null,
                shelter != null ? shelter.getName() : null,
                statusFor(user),
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

    private List<Animal> animalsAssignedToVeterinarian(User veterinarian) {
        if (veterinarian.getShelterId() == null) {
            return List.of();
        }
        return assignmentRepository.findAllByVeterinarianUserIdAndShelterIdAndActiveTrue(veterinarian.getId(), veterinarian.getShelterId()).stream()
                .map(VeterinarianAnimalAssignment::getAnimalId)
                .map(animalRepository::findById)
                .flatMap(java.util.Optional::stream)
                .filter(animal -> veterinarian.getShelterId().equals(animal.getShelterId()))
                .toList();
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

    private void validateInvite(String email, String firstName, String lastName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
    }

    private void validateRedeem(String code, String password, String confirmPassword) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Invitation code is required.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        if (confirmPassword != null && !confirmPassword.isBlank() && !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }
    }

    private User resolveVeterinarianForAdmin(Long veterinarianId, UUID adminShelterId) {
        if (veterinarianId == null) {
            throw new IllegalArgumentException("Veterinarian id is required.");
        }
        User veterinarian = userRepository.findById(veterinarianId)
                .orElseThrow(() -> new NoSuchElementException("Veterinarian not found with id: " + veterinarianId));
        if (!hasRole(veterinarian, RoleType.ROLE_VETERINARIAN)) {
            throw new NoSuchElementException("Veterinarian not found with id: " + veterinarianId);
        }
        if (!adminShelterId.equals(veterinarian.getShelterId())) {
            throw new IllegalArgumentException("Veterinarian does not belong to the current shelter.");
        }
        return veterinarian;
    }

    private Animal resolveAnimalForAdmin(UUID animalId, UUID adminShelterId) {
        if (animalId == null) {
            throw new IllegalArgumentException("Animal id is required.");
        }
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new NoSuchElementException("Animal not found with id: " + animalId));
        if (!adminShelterId.equals(animal.getShelterId())) {
            throw new IllegalArgumentException("Animal does not belong to the current shelter.");
        }
        return animal;
    }

    private String generateUniqueInvitationCode() {
        String code;
        do {
            code = INVITATION_CODE_PREFIX + randomCodeSuffix();
        } while (veterinaryInvitationRepository.existsByInvitationCode(code));
        return code;
    }

    private String randomCodeSuffix() {
        StringBuilder code = new StringBuilder(INVITATION_CODE_LENGTH);
        for (int i = 0; i < INVITATION_CODE_LENGTH; i++) {
            code.append(INVITATION_CODE_ALPHABET.charAt(SECURE_RANDOM.nextInt(INVITATION_CODE_ALPHABET.length())));
        }
        return code.toString();
    }

    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString();
    }

    private String statusFor(User user) {
        return veterinaryInvitationRepository.findFirstByVeterinarianUserIdAndStatusIgnoreCase(user.getId(), "PENDING")
                .map(invitation -> "PENDING")
                .orElse("ACTIVE");
    }

    private InvitedVeterinarianResource toInvitedVeterinarianResource(User user, Shelter shelter, VeterinaryInvitation invitation) {
        return new InvitedVeterinarianResource(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                RoleType.ROLE_VETERINARIAN.name(),
                user.getShelterId() != null ? user.getShelterId().toString() : null,
                shelter.getName(),
                invitation.getStatus(),
                invitation.getInvitationCode()
        );
    }

    private VeterinarianAnimalAssignmentResource toAssignmentResource(VeterinarianAnimalAssignment assignment) {
        return new VeterinarianAnimalAssignmentResource(
                assignment.getId().toString(),
                assignment.getVeterinarianUserId(),
                assignment.getAnimalId().toString(),
                assignment.getShelterId().toString(),
                assignment.isActive()
        );
    }
}

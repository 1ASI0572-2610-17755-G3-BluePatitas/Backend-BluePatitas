package com.bluepatitas.bluepatitasbackend.veterinary.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.entities.VeterinarianAnimalAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaVeterinarianAnimalAssignmentRepository extends JpaRepository<VeterinarianAnimalAssignment, UUID> {
    List<VeterinarianAnimalAssignment> findAllByVeterinarianUserIdAndActiveTrue(Long veterinarianUserId);

    List<VeterinarianAnimalAssignment> findAllByVeterinarianUserIdAndShelterIdAndActiveTrue(Long veterinarianUserId, UUID shelterId);

    List<VeterinarianAnimalAssignment> findAllByShelterIdAndActiveTrue(UUID shelterId);

    Optional<VeterinarianAnimalAssignment> findByVeterinarianUserIdAndAnimalIdAndActiveTrue(Long veterinarianUserId, UUID animalId);

    Optional<VeterinarianAnimalAssignment> findFirstByVeterinarianUserIdAndAnimalId(Long veterinarianUserId, UUID animalId);

    long countByVeterinarianUserIdAndActiveTrue(Long veterinarianUserId);
}

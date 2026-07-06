package com.bluepatitas.bluepatitasbackend.veterinary.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "veterinarian_animal_assignments")
@Getter
@NoArgsConstructor
public class VeterinarianAnimalAssignment {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "veterinarian_user_id", nullable = false)
    private Long veterinarianUserId;

    @Column(name = "animal_id", nullable = false)
    private UUID animalId;

    @Column(name = "shelter_id", nullable = false)
    private UUID shelterId;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    public VeterinarianAnimalAssignment(UUID id, Long veterinarianUserId, UUID animalId, UUID shelterId) {
        if (id == null) {
            throw new IllegalArgumentException("Assignment id is required.");
        }
        if (veterinarianUserId == null) {
            throw new IllegalArgumentException("Veterinarian user id is required.");
        }
        if (animalId == null) {
            throw new IllegalArgumentException("Animal id is required.");
        }
        if (shelterId == null) {
            throw new IllegalArgumentException("Shelter id is required.");
        }
        this.id = id;
        this.veterinarianUserId = veterinarianUserId;
        this.animalId = animalId;
        this.shelterId = shelterId;
        this.assignedAt = LocalDateTime.now();
        this.active = true;
    }

    public void reactivate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}

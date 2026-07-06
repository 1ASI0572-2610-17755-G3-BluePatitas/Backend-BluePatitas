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
@Table(name = "veterinary_invitations")
@Getter
@NoArgsConstructor
public class VeterinaryInvitation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "veterinarian_user_id", nullable = false)
    private Long veterinarianUserId;

    @Column(name = "shelter_id", nullable = false)
    private UUID shelterId;

    @Column(name = "invitation_code", nullable = false, unique = true, length = 32)
    private String invitationCode;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public VeterinaryInvitation(UUID id, Long veterinarianUserId, UUID shelterId, String invitationCode) {
        if (id == null) {
            throw new IllegalArgumentException("Invitation id is required.");
        }
        if (veterinarianUserId == null) {
            throw new IllegalArgumentException("Veterinarian user id is required.");
        }
        if (shelterId == null) {
            throw new IllegalArgumentException("Shelter id is required.");
        }
        if (invitationCode == null || invitationCode.isBlank()) {
            throw new IllegalArgumentException("Invitation code is required.");
        }
        this.id = id;
        this.veterinarianUserId = veterinarianUserId;
        this.shelterId = shelterId;
        this.invitationCode = invitationCode;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public void markUsed() {
        this.status = "USED";
        this.usedAt = LocalDateTime.now();
    }
}

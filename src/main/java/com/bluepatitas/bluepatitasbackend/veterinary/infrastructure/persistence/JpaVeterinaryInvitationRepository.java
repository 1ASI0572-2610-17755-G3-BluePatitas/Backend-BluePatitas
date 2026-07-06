package com.bluepatitas.bluepatitasbackend.veterinary.infrastructure.persistence;

import com.bluepatitas.bluepatitasbackend.veterinary.domain.model.entities.VeterinaryInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaVeterinaryInvitationRepository extends JpaRepository<VeterinaryInvitation, UUID> {
    Optional<VeterinaryInvitation> findByInvitationCode(String invitationCode);

    Optional<VeterinaryInvitation> findFirstByVeterinarianUserIdAndStatusIgnoreCase(Long veterinarianUserId, String status);

    boolean existsByInvitationCode(String invitationCode);
}

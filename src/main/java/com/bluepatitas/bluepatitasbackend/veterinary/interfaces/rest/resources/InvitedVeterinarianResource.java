package com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources;

public record InvitedVeterinarianResource(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        String shelterId,
        String shelterName,
        String status,
        String invitationCode) {
}

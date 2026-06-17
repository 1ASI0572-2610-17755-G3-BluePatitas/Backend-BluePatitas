package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String firstName, String lastName, String email, String token, String shelterId) {
}

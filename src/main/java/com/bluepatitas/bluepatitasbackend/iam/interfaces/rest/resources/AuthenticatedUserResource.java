package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources;

import java.util.List;

public record AuthenticatedUserResource(
        Long id,
        String firstName,
        String lastName,
        String email,
        String token,
        String shelterId,
        String role,
        List<String> roles,
        String shelterName,
        Boolean onboardingCompleted) {
}

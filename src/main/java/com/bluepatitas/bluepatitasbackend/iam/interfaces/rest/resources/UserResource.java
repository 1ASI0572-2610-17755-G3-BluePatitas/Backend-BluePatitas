package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources;

import java.util.List;

public record UserResource(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        List<String> roles) {
}

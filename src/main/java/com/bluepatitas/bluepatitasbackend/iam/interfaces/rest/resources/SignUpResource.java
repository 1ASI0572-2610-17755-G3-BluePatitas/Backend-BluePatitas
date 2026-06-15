package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources;

public record SignUpResource(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String password,
        String role
) { }

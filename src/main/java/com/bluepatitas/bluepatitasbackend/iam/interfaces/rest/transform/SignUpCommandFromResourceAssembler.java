package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.commands.SignUpCommand;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.Role;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.valueobjects.RoleType;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.SignUpResource;

import java.util.List;

public class SignUpCommandFromResourceAssembler {
    private static final String VETERINARIAN_SIGNUP_MESSAGE = "Veterinarians must be invited by a shelter administrator.";

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var roles = List.of(new Role(resolvePublicSignupRole(resource.role())));
        return new SignUpCommand(
                resource.firstName(),
                resource.lastName(),
                resource.email(),
                resource.phoneNumber(),
                resource.password(),
                roles);
    }

    private static RoleType resolvePublicSignupRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return RoleType.ROLE_SHELTER_ADMIN;
        }
        String normalized = rawRole.trim().toUpperCase();
        if ("SHELTER_ADMIN".equals(normalized) || "ROLE_SHELTER_ADMIN".equals(normalized)) {
            return RoleType.ROLE_SHELTER_ADMIN;
        }
        if ("VETERINARIAN".equals(normalized) || "ROLE_VETERINARIAN".equals(normalized)) {
            throw new IllegalArgumentException(VETERINARIAN_SIGNUP_MESSAGE);
        }
        throw new IllegalArgumentException("Unknown role: " + rawRole);
    }
}

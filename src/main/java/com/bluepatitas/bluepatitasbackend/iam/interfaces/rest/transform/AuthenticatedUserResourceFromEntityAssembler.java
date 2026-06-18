package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.aggregates.User;
import com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.Role;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;

import java.util.List;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token, String shelterName) {
        List<String> roles = user.getRoles().stream()
                .map(AuthenticatedUserResourceFromEntityAssembler::toClientRole)
                .sorted()
                .toList();
        String primaryRole = roles.contains("SHELTER_ADMIN")
                ? "SHELTER_ADMIN"
                : roles.stream().findFirst().orElse(null);

        return new AuthenticatedUserResource(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                token,
                user.getShelterId() != null ? user.getShelterId().toString() : null,
                primaryRole,
                roles,
                shelterName,
                user.getShelterId() != null
        );
    }

    private static String toClientRole(Role role) {
        String roleName = role.getStringName();
        return roleName.startsWith("ROLE_") ? roleName.substring("ROLE_".length()) : roleName;
    }
}

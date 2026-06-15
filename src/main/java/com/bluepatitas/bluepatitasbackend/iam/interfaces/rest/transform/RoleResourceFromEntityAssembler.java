package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.Role;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.RoleResource;

public class RoleResourceFromEntityAssembler {
    public static RoleResource toResourceFromEntity(Role role) {
        return new RoleResource(role.getId(), role.getStringName());
    }
}

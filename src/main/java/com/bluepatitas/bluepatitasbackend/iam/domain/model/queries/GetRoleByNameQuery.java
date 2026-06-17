package com.bluepatitas.bluepatitasbackend.iam.domain.model.queries;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.valueobjects.RoleType;

/**
 * Get role by name query
 * <p>
 *     This class represents the query to get a role by its name.
 * </p>
 * @param name the name of the role
 * @see com.bluepatitas.bluepatitasbackend.iam.domain.model.valueobjects.RoleType
 */
public record GetRoleByNameQuery(RoleType name) {
}

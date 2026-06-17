package com.bluepatitas.bluepatitasbackend.iam.domain.services;

/**
 * Role command service
 * <p>
 *     This interface represents the service to handle role commands.
 * </p>
 */
import com.bluepatitas.bluepatitasbackend.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    /**
     * Handle seed roles command
     * @param command the {@link SeedRolesCommand} command
     *
     */
    void handle(SeedRolesCommand command);
}

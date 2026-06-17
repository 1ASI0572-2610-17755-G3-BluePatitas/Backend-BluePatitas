package com.bluepatitas.bluepatitasbackend.iam.domain.model.commands;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.Role;

import java.util.List;

/**
 * Sign up command
 * <p>
 * This class represents the command to sign up a user.
 * </p>
 *
 * @param firstName the first name of the user
 * @param lastName the last name of the user
 * @param email the email of the user
 * @param phoneNumber the phone number of the user
 * @param password the password of the user
 * @param roles the roles of the user
 *
 * @see com.bluepatitas.bluepatitasbackend.iam.domain.model.aggregates.User
 */
public record SignUpCommand(
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String password,
        List<Role> roles) {
}

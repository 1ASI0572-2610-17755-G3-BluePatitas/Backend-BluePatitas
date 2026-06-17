package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.commands.SignInCommand;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource signInResource) {
        return new SignInCommand(signInResource.email(), signInResource.password());
    }
}
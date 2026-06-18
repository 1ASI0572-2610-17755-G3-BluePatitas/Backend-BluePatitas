package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.iam.domain.services.UserCommandService;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.SignInResource;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.SignUpResource;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.UserResource;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.aggregates.Shelter;
import com.bluepatitas.bluepatitasbackend.monitoring.domain.model.repositories.ShelterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * AuthenticationController
 * <p>
 *     This controller is responsible for handling authentication requests.
 *     It exposes two endpoints:
 *     <ul>
 *         <li>POST /api/v1/authentication/sign-in</li>
 *         <li>POST /api/v1/authentication/sign-up</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;
    private final ShelterRepository shelterRepository;

    public AuthenticationController(UserCommandService userCommandService, ShelterRepository shelterRepository) {
        this.userCommandService = userCommandService;
        this.shelterRepository = shelterRepository;
    }

    /**
     * Handles the sign-in request.
     * @param signInResource the sign-in request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Sign-in with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully."),
            @ApiResponse(responseCode = "404", description = "User not found.")})
    public ResponseEntity<?> signIn(@RequestBody SignInResource signInResource) {
        try {
            var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
            var authenticatedUser = userCommandService.handle(signInCommand);
            if (authenticatedUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid email or password."));
            }
            var user = authenticatedUser.get().getLeft();
            var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                    user,
                    authenticatedUser.get().getRight(),
                    resolveShelterName(user.getShelterId()));
            return ResponseEntity.ok(authenticatedUserResource);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password."));
        }
    }

    /**
     * Handles the sign-up request.
     * @param signUpResource the sign-up request body.
     * @return the created user resource.
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Sign-up", description = "Sign-up with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")})
    public ResponseEntity<?> signUp(@RequestBody SignUpResource signUpResource) {
        try {
            var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
            var user = userCommandService.handle(signUpCommand);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
            return new ResponseEntity<>(userResource, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }

    }

    private String resolveShelterName(UUID shelterId) {
        if (shelterId == null) {
            return null;
        }
        return shelterRepository.findById(shelterId)
                .map(Shelter::getName)
                .orElse(null);
    }
}

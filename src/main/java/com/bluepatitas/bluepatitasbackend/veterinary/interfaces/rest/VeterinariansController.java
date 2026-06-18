package com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.veterinary.application.services.VeterinaryUserService;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.InvitedVeterinarianResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinarianResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinaryAnimalResource;
import com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources.VeterinaryDashboardResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/veterinary")
@RequiredArgsConstructor
@Tag(name = "Veterinary - Users", description = "Endpoints for veterinary user contracts")
public class VeterinariansController {
    private final VeterinaryUserService veterinaryUserService;

    @GetMapping("/veterinarians")
    @PreAuthorize("hasAuthority('ROLE_SHELTER_ADMIN')")
    @Operation(summary = "List veterinarians", description = "Lists users with ROLE_VETERINARIAN for the current shelter.")
    public ResponseEntity<List<VeterinarianResource>> getVeterinarians() {
        return ResponseEntity.ok(veterinaryUserService.getVeterinarians());
    }

    @PostMapping("/veterinarians/invite")
    @PreAuthorize("hasAuthority('ROLE_SHELTER_ADMIN')")
    @Operation(summary = "Invite veterinarian", description = "Creates an active veterinarian user for the current shelter. Email delivery is pending.")
    public ResponseEntity<?> inviteVeterinarian(@RequestBody InviteVeterinarianRequest request) {
        try {
            InvitedVeterinarianResource invited = veterinaryUserService.inviteVeterinarian(
                    request.email(),
                    request.firstName(),
                    request.lastName(),
                    request.password()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(invited);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/me/dashboard")
    @PreAuthorize("hasAuthority('ROLE_VETERINARIAN')")
    @Operation(summary = "Veterinary dashboard", description = "Returns a minimal dashboard for the authenticated veterinarian.")
    public ResponseEntity<VeterinaryDashboardResource> getMyDashboard() {
        return ResponseEntity.ok(veterinaryUserService.getMyDashboard());
    }

    @GetMapping("/me/animals")
    @PreAuthorize("hasAuthority('ROLE_VETERINARIAN')")
    @Operation(summary = "Veterinary animals", description = "Returns animals from the authenticated veterinarian's shelter.")
    public ResponseEntity<List<VeterinaryAnimalResource>> getMyAnimals() {
        return ResponseEntity.ok(veterinaryUserService.getMyAnimals());
    }

    public record InviteVeterinarianRequest(
            String email,
            String firstName,
            String lastName,
            String password
    ) {
    }
}

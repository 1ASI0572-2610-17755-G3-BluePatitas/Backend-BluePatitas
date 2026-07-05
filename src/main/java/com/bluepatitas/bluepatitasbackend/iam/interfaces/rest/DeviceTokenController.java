package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.entities.DeviceToken;
import com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories.DeviceTokenRepository;
import com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources.RegisterDeviceTokenResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/device-tokens")
@Tag(name = "Device Tokens", description = "Device Token Management Endpoints")
public class DeviceTokenController {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceTokenController(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    @PostMapping
    public ResponseEntity<?> registerToken(
            @PathVariable Long userId,
            @RequestBody @Valid RegisterDeviceTokenResource resource) {
        
        // If the token already exists, we could just update the userId or ignore
        deviceTokenRepository.findByToken(resource.getToken()).ifPresentOrElse(
                existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        existing.setUserId(userId);
                        deviceTokenRepository.save(existing);
                    }
                },
                () -> {
                    DeviceToken newToken = new DeviceToken(userId, resource.getToken(), resource.getDeviceType());
                    deviceTokenRepository.save(newToken);
                }
        );

        return ResponseEntity.ok().build();
    }
}

package com.bluepatitas.bluepatitasbackend.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDeviceTokenResource {
    @NotBlank
    private String token;
    private String deviceType;
}

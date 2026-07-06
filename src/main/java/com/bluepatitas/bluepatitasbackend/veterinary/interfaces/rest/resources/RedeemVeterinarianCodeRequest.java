package com.bluepatitas.bluepatitasbackend.veterinary.interfaces.rest.resources;

public record RedeemVeterinarianCodeRequest(
        String code,
        String password,
        String confirmPassword) {
}

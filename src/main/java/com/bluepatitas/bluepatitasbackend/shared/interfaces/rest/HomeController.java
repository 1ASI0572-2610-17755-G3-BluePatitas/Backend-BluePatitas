package com.bluepatitas.bluepatitasbackend.shared.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Home", description = "Root and health status endpoints")
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Get application health status", description = "Returns a simple JSON showing that the backend service is up and running.")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "BluePatitas Backend is running!"
        ));
    }
}

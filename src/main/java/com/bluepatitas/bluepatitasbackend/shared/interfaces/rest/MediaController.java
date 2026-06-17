package com.bluepatitas.bluepatitasbackend.shared.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Media", description = "Endpoints for file and media uploads")
public class MediaController {

    @PostMapping("/upload")
    @Operation(summary = "Upload a media file", description = "Uploads a raw image or file to the server and returns its relative access path.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully."),
            @ApiResponse(responseCode = "400", description = "File is empty or invalid."),
            @ApiResponse(responseCode = "500", description = "Internal server error saving file.")
    })
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path uploadsDir = Paths.get("uploads").toAbsolutePath();
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
            }
            Path destinationPath = uploadsDir.resolve(uniqueFilename);
            
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
            
            String fileUrl = "/uploads/" + uniqueFilename;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not save file: " + e.getMessage()));
        }
    }
}

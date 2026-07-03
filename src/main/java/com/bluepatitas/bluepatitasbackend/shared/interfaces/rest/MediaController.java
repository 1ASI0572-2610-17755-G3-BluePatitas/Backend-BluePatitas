package com.bluepatitas.bluepatitasbackend.shared.interfaces.rest;

import com.bluepatitas.bluepatitasbackend.shared.application.services.MediaService;
import com.bluepatitas.bluepatitasbackend.shared.application.services.MediaService.MediaUploadException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/media")
@Tag(name = "Media", description = "Endpoints for file and media uploads")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a media file", description = "Uploads an image to Cloudinary and returns its secure URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully."),
            @ApiResponse(responseCode = "400", description = "File is empty or invalid."),
            @ApiResponse(responseCode = "500", description = "Internal server error uploading file.")
    })
    public ResponseEntity<Map<String, String>> uploadFile(
            @Parameter(
                    description = "Image file to upload",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        try {
            var uploadedMedia = mediaService.uploadImage(file);
            return ResponseEntity.ok(Map.of(
                    "url", uploadedMedia.url(),
                    "secureUrl", uploadedMedia.secureUrl(),
                    "publicId", uploadedMedia.publicId()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (MediaUploadException ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage()));
        }
    }
}

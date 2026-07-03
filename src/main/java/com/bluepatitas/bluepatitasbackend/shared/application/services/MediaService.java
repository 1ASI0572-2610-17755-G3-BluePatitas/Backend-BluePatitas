package com.bluepatitas.bluepatitasbackend.shared.application.services;

import com.bluepatitas.bluepatitasbackend.shared.infrastructure.config.CloudinaryConfig.CloudinaryCredentials;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class MediaService {
    private static final String ANIMALS_FOLDER = "bluepatitas/animals";

    private final Cloudinary cloudinary;
    private final CloudinaryCredentials credentials;

    public MediaService(Cloudinary cloudinary, CloudinaryCredentials credentials) {
        this.cloudinary = cloudinary;
        this.credentials = credentials;
    }

    public UploadedMedia uploadImage(MultipartFile file) {
        validateImage(file);
        validateCloudinaryConfiguration();

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", ANIMALS_FOLDER,
                    "resource_type", "image",
                    "public_id", UUID.randomUUID().toString(),
                    "overwrite", false
            ));

            String secureUrl = valueAsString(uploadResult.get("secure_url"));
            String publicId = valueAsString(uploadResult.get("public_id"));
            if (isBlank(secureUrl) || isBlank(publicId)) {
                throw new MediaUploadException("Cloudinary upload did not return the expected image metadata.");
            }

            return new UploadedMedia(secureUrl, secureUrl, publicId);
        } catch (IOException ex) {
            throw new MediaUploadException("Could not upload image to Cloudinary.");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    private void validateCloudinaryConfiguration() {
        if (!credentials.isConfigured()) {
            throw new MediaUploadException("Cloudinary credentials are not configured.");
        }
    }

    private String valueAsString(Object value) {
        return value == null ? null : value.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record UploadedMedia(String url, String secureUrl, String publicId) {
    }

    public static class MediaUploadException extends RuntimeException {
        public MediaUploadException(String message) {
            super(message);
        }
    }
}

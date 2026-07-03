package com.bluepatitas.bluepatitasbackend.shared.infrastructure.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public CloudinaryCredentials cloudinaryCredentials(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret) {
        return new CloudinaryCredentials(cloudName, apiKey, apiSecret);
    }

    @Bean
    public Cloudinary cloudinary(CloudinaryCredentials credentials) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", credentials.cloudName(),
                "api_key", credentials.apiKey(),
                "api_secret", credentials.apiSecret(),
                "secure", true
        ));
    }

    public record CloudinaryCredentials(String cloudName, String apiKey, String apiSecret) {

        public boolean isConfigured() {
            return hasText(cloudName) && hasText(apiKey) && hasText(apiSecret);
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}

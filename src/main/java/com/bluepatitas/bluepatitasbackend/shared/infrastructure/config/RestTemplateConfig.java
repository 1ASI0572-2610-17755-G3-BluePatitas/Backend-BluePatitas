package com.bluepatitas.bluepatitasbackend.shared.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplateConfig
 * <p>
 * Shared infrastructure configuration that registers a {@link RestTemplate}
 * bean for use by any bounded context that needs to make outbound HTTP calls.
 * Currently used by:
 * <ul>
 *   <li>{@code DispenserDeviceService} — Edge API Gateway integration (Feeding BC)</li>
 * </ul>
 * </p>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Provides a default {@link RestTemplate} instance.
     * For production use, consider customising connection timeouts,
     * retry policies, or adding an interceptor for distributed tracing.
     *
     * @return a plain RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

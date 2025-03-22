package com.amalvadkar.lms.gateway;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "lms.cors")
@Validated
public record CorsProperties(

        @NotEmpty(message = "allowedOrigins property should not be null or empty")
        List<String> allowedOrigins,
        
        @NotEmpty(message = "allowedMethods property should not be null or empty")
        List<String> allowedMethods,

        @NotEmpty(message = "allowedHeaders property should not be null or empty")
        List<String> allowedHeaders,

        @NotNull(message = "maxAge property should not be null")
        Long maxAge,

        @NotEmpty(message = "exposedHeaders property should not be null or empty")
        List<String> exposedHeaders
) {
}
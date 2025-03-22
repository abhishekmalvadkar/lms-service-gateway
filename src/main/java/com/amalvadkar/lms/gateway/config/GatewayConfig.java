package com.amalvadkar.lms.gateway.config;

import com.amalvadkar.lms.gateway.CorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
public class GatewayConfig {

    @Bean
    public CorsWebFilter corsWebFilter(CorsProperties corsProps) {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(corsProps.allowedOrigins());
        corsConfig.setAllowedMethods(corsProps.allowedMethods());
        corsConfig.setAllowedHeaders(corsProps.allowedHeaders());
        corsConfig.setMaxAge(corsProps.maxAge());
        corsConfig.setExposedHeaders(corsProps.exposedHeaders());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

}
package com.amalvadkar.lms.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lms")
public record ApplicationProperties(String verifyTokenUrl) {
}

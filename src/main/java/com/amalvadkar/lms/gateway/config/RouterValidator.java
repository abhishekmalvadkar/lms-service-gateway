package com.amalvadkar.lms.gateway.config;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator {

    private static final List<String> OPEN_API_END_POINTS = List.of(
            "/api/auth/create-account",
            "/api/auth/verify-account",
            "/api/auth/sign-in",
            "/api/auth/verify-otp",
            "/api/auth/verify-token"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_END_POINTS.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}

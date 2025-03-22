package com.amalvadkar.lms.gateway;

import com.amalvadkar.lms.gateway.common.AbstractIT;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.nullValue;

class CorsIntegrationTest extends AbstractIT {

    @Test
    void should_allow_cors_for_preflight_request() {
        given()
            .header("Origin", "https://dev-lms-amalvadkar.netlify.app")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "Authorization")
            .when()
            .options("/api/lms/test")
            .then()
            .statusCode(200)
            .header("Access-Control-Allow-Origin", "https://dev-lms-amalvadkar.netlify.app")
            .header("Access-Control-Allow-Methods", "GET")
            .header("Access-Control-Allow-Headers", "Authorization")
            .header("Access-Control-Max-Age", "43200");
    }

    @Test
    void should_allow_cors_for_simple_request() {
        given()
            .header("Origin", "https://qa-lms.com")
            .when()
            .get("/api/lms/test")
            .then()
            .statusCode(500) // Expected connection refused since no real downstream service
            .header("Access-Control-Allow-Origin", "https://qa-lms.com");
    }

    @Test
    void should_reject_cors_for_unknown_origin() {
        given()
            .header("Origin", "https://unauthorized.com")
            .when()
            .get("/api/lms/test")
            .then()
            .statusCode(403) // Expected since other origin not allowed
            .header("Access-Control-Allow-Origin", nullValue());
    }
}
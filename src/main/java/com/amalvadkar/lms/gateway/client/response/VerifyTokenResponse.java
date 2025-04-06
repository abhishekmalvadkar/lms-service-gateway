package com.amalvadkar.lms.gateway.client.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record VerifyTokenResponse(String userId,
                                  String roleId,
                                  String device,
                                  int status,
                                  String message
) {
    public VerifyTokenResponse(int status, String message) {
        this(null, null, null, status, message);
    }
}
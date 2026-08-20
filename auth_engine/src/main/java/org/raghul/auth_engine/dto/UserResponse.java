package org.raghul.auth_engine.dto;

public record UserResponse(
        Integer userId,
        String name,
        String email,
        Integer tenantId
) {
}


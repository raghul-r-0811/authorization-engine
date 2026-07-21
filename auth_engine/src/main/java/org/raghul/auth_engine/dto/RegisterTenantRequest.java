package org.raghul.auth_engine.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterTenantRequest(@NotBlank String tenantName) {
}

package org.raghul.auth_engine.dto;

import jakarta.validation.constraints.NotNull;

public record RegisterTenantRequest(@NotNull String tenantName) {
}

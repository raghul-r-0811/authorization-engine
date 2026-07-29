package org.raghul.auth_engine.dto;

public record AssignRoleRequest(Integer userId,
                                Integer roleId,
                                Integer tenantId) {
}

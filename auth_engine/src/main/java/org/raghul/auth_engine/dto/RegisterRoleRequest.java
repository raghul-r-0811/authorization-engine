package org.raghul.auth_engine.dto;

import org.raghul.auth_engine.entity.ScopeType;

public record RegisterRoleRequest(String roleName, String description, Integer tenantId, ScopeType scope) {

}

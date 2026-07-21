package org.raghul.auth_engine.dto;

public record RegisterRoleRequest(String roleName,String description,Integer tenantId,String scope) {

}

package org.raghul.auth_engine.dto;

public record RolePermissionResponse(Integer roleId,String roleName, Integer permissionId, String permissionName) {
}

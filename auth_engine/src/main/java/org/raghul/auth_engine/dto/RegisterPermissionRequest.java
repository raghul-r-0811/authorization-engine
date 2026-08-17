package org.raghul.auth_engine.dto;

import org.raghul.auth_engine.entity.PermissionApplicability;

public record RegisterPermissionRequest(
        String description,
        String permissionName,
        PermissionApplicability applicability
) {
}

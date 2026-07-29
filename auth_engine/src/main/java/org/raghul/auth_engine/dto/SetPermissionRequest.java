package org.raghul.auth_engine.dto;

import java.util.Set;

public record SetPermissionRequest(Integer roleId, Set<String> permissionSet) {
}

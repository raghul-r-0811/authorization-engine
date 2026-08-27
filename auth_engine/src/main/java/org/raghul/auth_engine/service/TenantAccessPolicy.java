package org.raghul.auth_engine.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


/**
 * This class will be used to set boundaries to pervent for cross tenent actions
 * **/
@Service
public class TenantAccessPolicy {

    public void verifyCanAccessTenant(Integer targetTenantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_SUPER_ADMIN"));

        // SuperAdmin is explicitly permitted to operate across tenants.
        if (isSuperAdmin) {
            return;
        }

        Integer callerTenantId = (Integer) authentication.getDetails();
        if (callerTenantId == null ||
                !callerTenantId.equals(targetTenantId)) {
            throw new AccessDeniedException(
                    "Cross-tenant operation is not allowed"
            );
        }
    }


    // When one platform user try to act on another platform user
    public void verifyCanAccessPlatform() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }
        boolean isSuperAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            throw new AccessDeniedException(
                    "Only SuperAdmin can perform platform-level operations"
            );
        }
    }
}

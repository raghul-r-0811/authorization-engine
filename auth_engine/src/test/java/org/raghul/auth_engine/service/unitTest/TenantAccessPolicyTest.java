package org.raghul.auth_engine.service.unitTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raghul.auth_engine.service.TenantAccessPolicy;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantAccessPolicyTest {

    private static final Integer CALLER_TENANT_ID = 10;
    private static final Integer ANOTHER_TENANT_ID = 20;

    private TenantAccessPolicy tenantAccessPolicy;

    @BeforeEach
    void setUp() {
        tenantAccessPolicy = new TenantAccessPolicy();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {SecurityContextHolder.clearContext();}

    @Test
    void verifyCanAccessTenant_whenAuthenticationIsMissing_throwsAccessDeniedException() {
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> tenantAccessPolicy.verifyCanAccessTenant(CALLER_TENANT_ID));
        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void verifyCanAccessTenant_whenAuthenticationIsNotAuthenticated_throwsAccessDeniedException() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user@example.com", null);
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> tenantAccessPolicy.verifyCanAccessTenant(CALLER_TENANT_ID));
        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void verifyCanAccessTenant_whenSuperAdminAccessesAnyTenant_doesNotThrowException() {
        setAuthentication("superadmin@example.com", null, "ROLE_SUPER_ADMIN");
        assertDoesNotThrow(() -> tenantAccessPolicy.verifyCanAccessTenant(CALLER_TENANT_ID));
        assertDoesNotThrow(() -> tenantAccessPolicy.verifyCanAccessTenant(ANOTHER_TENANT_ID));
    }

    @Test
    void verifyCanAccessTenant_whenTenantUserAccessesOwnTenant_doesNotThrowException() {
        setAuthentication("tenant-admin@example.com", CALLER_TENANT_ID, "ROLE_TENANT_ADMIN");
        assertDoesNotThrow(() -> tenantAccessPolicy
                .verifyCanAccessTenant(CALLER_TENANT_ID));
    }

    @Test
    void verifyCanAccessTenant_whenTenantUserAccessesAnotherTenant_throwsAccessDeniedException() {
        setAuthentication("tenant-admin@example.com", CALLER_TENANT_ID, "ROLE_TENANT_ADMIN");
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> tenantAccessPolicy.verifyCanAccessTenant(ANOTHER_TENANT_ID));
        assertEquals("Cross-tenant operation is not allowed"
                , exception.getMessage());
    }

    @Test
    void verifyCanAccessTenant_whenNonSuperAdminHasNoTenantInDetails_throwsAccessDeniedException() {
        setAuthentication("tenant-admin@example.com",
                null, "ROLE_TENANT_ADMIN");
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> tenantAccessPolicy.verifyCanAccessTenant(CALLER_TENANT_ID));
        assertEquals("Cross-tenant operation is not allowed", exception.getMessage());
    }

    @Test
    void verifyCanAccessPlatform_whenAuthenticationIsMissing_throwsAccessDeniedException() {
        AccessDeniedException exception = assertThrows(AccessDeniedException.class
                ,() -> tenantAccessPolicy.verifyCanAccessPlatform());
        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void verifyCanAccessPlatform_whenAuthenticationIsNotAuthenticated_throwsAccessDeniedException() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user@example.com", null);
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AccessDeniedException exception = assertThrows(AccessDeniedException.class
                ,() -> tenantAccessPolicy.verifyCanAccessPlatform());

        assertEquals("Authentication is required", exception.getMessage());
    }

    @Test
    void verifyCanAccessPlatform_whenSuperAdminAccessesPlatform_doesNotThrowException() {
        setAuthentication("superadmin@example.com", null, "ROLE_SUPER_ADMIN");

        assertDoesNotThrow(() -> tenantAccessPolicy.verifyCanAccessPlatform());
    }

    @Test
    void verifyCanAccessPlatform_whenTenantUserAccessesPlatform_throwsAccessDeniedException() {
        setAuthentication("tenant-admin@example.com", CALLER_TENANT_ID, "ROLE_TENANT_ADMIN");

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> tenantAccessPolicy.verifyCanAccessPlatform());

        assertEquals("Only SuperAdmin can perform platform-level operations", exception.getMessage());
    }

    @Test
    void verifyCanAccessPlatform_whenAuthenticatedUserHasNoAuthorities_throwsAccessDeniedException() {
        setAuthentication("user@example.com", CALLER_TENANT_ID);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> tenantAccessPolicy.verifyCanAccessPlatform());

        assertEquals("Only SuperAdmin can perform platform-level operations", exception.getMessage());
    }

    private void setAuthentication(String username, Integer tenantId, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList();

        TestingAuthenticationToken authentication = new TestingAuthenticationToken(username, null, grantedAuthorities);
        authentication.setDetails(tenantId);
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
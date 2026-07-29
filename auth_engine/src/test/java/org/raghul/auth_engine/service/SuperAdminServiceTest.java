package org.raghul.auth_engine.service;



import org.junit.jupiter.api.Test;
import org.raghul.auth_engine.dto.RegisterTenantRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class SuperAdminServiceTest {
    @Autowired
    SuperAdminService superAdminService;

    private RegisterTenantRequest request() {
        RegisterTenantRequest request = new RegisterTenantRequest("Test2 Tenant");
        return request;
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void addTenant_shouldAllowSuperAdmin() {
        assertDoesNotThrow(() -> superAdminService.addNewTenant(request()));
    }

    @Test
    @WithMockUser(roles ="TENANT_ADMIN")
    void addTenant_shouldDenyTenantAdmin(){
        assertThrows(AccessDeniedException.class,()->superAdminService.addNewTenant(request()));
    }

    @Test
    @WithMockUser(roles ="USER")
    void addTenant_shouldDenyUser(){
        assertThrows(AccessDeniedException.class,()->superAdminService.addNewTenant(request()));
    }
    @Test
    @WithAnonymousUser
    void addTenant_shouldDenyAnonymousUser(){
        assertThrows(AccessDeniedException.class,()->superAdminService.addNewTenant(request()));
    }

    @Test
    void someTest(){
        System.out.println("this ran");
    }
}

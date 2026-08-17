package org.raghul.auth_engine;

import org.raghul.auth_engine.dto.RegisterPermissionRequest;
import org.raghul.auth_engine.entity.*;
import org.raghul.auth_engine.repository.PermissionRepo;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.TenantRepo;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.repository.UserRoleRepo;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataBootstrap implements ApplicationRunner {

    private final PermissionRepo permissionRepo;
    private final RolesRepo rolesRepo;
    private final UserRepo userRepo;
    private final UserRoleRepo userRoleRepo;
    private final TenantRepo tenantRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        List<RegisterPermissionRequest> permissions = List.of(
                new RegisterPermissionRequest("Create a tenant", "TENANT_CREATE", PermissionApplicability.PLATFORM_ONLY),
                new RegisterPermissionRequest("View tenant details", "TENANT_READ", PermissionApplicability.PLATFORM_ONLY),
                new RegisterPermissionRequest("Update tenant details", "TENANT_UPDATE", PermissionApplicability.PLATFORM_ONLY),
                new RegisterPermissionRequest("Suspend or reactivate a tenant", "TENANT_SUSPEND", PermissionApplicability.PLATFORM_ONLY),

                new RegisterPermissionRequest("Create users in a tenant", "USER_CREATE", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("View users in a tenant", "USER_READ", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("Update users in a tenant", "USER_UPDATE", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("Delete users in a tenant", "USER_DELETE", PermissionApplicability.BOTH),

                new RegisterPermissionRequest("Create tenant roles", "ROLE_CREATE", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("View tenant roles", "ROLE_READ", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("Update tenant roles", "ROLE_UPDATE", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("Delete tenant roles", "ROLE_DELETE", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("Assign roles to users", "ROLE_ASSIGN", PermissionApplicability.BOTH),
                new RegisterPermissionRequest("Set permissions for roles", "ROLE_PERMISSION_SET", PermissionApplicability.BOTH),

                new RegisterPermissionRequest("View authorization audit logs", "AUDIT_LOG_READ", PermissionApplicability.PLATFORM_ONLY)
        );

        List<PermissionEntity> savedPermissions = permissions.stream()
                .map(request -> permissionRepo.findByPermissionName(request.permissionName())
                        .orElseGet(() -> {
                            PermissionEntity permission = new PermissionEntity();
                            permission.setPermissionName(request.permissionName());
                            permission.setDescription(request.description());
                            permission.setApplicability(request.applicability());
                            return permissionRepo.save(permission);
                        }))
                .toList();

        Set<String> tenantAdminPermissionNames = Set.of(
                "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
                "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE",
                "ROLE_ASSIGN", "ROLE_PERMISSION_SET"
        );

        List<PermissionEntity> tenantAdminPermissions = savedPermissions.stream()
                .filter(permission -> tenantAdminPermissionNames.contains(permission.getPermissionName()))
                .toList();

        RolesEntity superAdminRole = new RolesEntity();
        superAdminRole.setRoleName("SUPER_ADMIN");
        superAdminRole.setDescription("Platform-level administrator");
        superAdminRole.setScopeType(ScopeType.PLATFORM);
        superAdminRole.setTenant(null);

        for (PermissionEntity permission : savedPermissions) {
            RolePermissionEntity rolePermission = new RolePermissionEntity();
            rolePermission.setRole(superAdminRole);
            rolePermission.setPermission(permission);
            superAdminRole.getRolePermission().add(rolePermission);
        }

        superAdminRole = rolesRepo.save(superAdminRole);

        UserEntity superAdmin = new UserEntity();
        superAdmin.setName("Platform Super Admin");
        superAdmin.setEmail("RaghulSuper@gmail.com");
        superAdmin.setPassword(passwordEncoder.encode("supersecret123456"));
        superAdmin.setTenant(null);

        superAdmin = userRepo.save(superAdmin);

        UserRoleEntity assignment = new UserRoleEntity();
        assignment.setUser(superAdmin);
        assignment.setRole(superAdminRole);
        assignment.setTenant(null);
        userRoleRepo.save(assignment);

        TenantEntity orgA = new TenantEntity();
        orgA.setTenantName("orgA");
        orgA = tenantRepo.save(orgA);

        TenantEntity orgB = new TenantEntity();
        orgB.setTenantName("orgB");
        orgB = tenantRepo.save(orgB);

        TenantEntity orgC = new TenantEntity();
        orgC.setTenantName("orgC");
        orgC = tenantRepo.save(orgC);

        RolesEntity orgATenantAdminRole = createTenantAdminRole(orgA, tenantAdminPermissions);
        createUnprivilegedTenantRole(orgA, "Junior Developer", "Entry-level developer role in orgA");
        createUnprivilegedTenantRole(orgA, "Senior Developer", "Senior developer role in orgA");

        RolesEntity orgBTenantAdminRole = createTenantAdminRole(orgB, tenantAdminPermissions);
        createUnprivilegedTenantRole(orgB, "Engineer", "Engineer role in orgB");
        createUnprivilegedTenantRole(orgB, "Senior Engineer", "Senior engineer role in orgB");

        RolesEntity orgCTenantAdminRole = createTenantAdminRole(orgC, tenantAdminPermissions);
        createUnprivilegedTenantRole(orgC, "Associate Consultant", "Entry-level consultant role in orgC");
        createUnprivilegedTenantRole(orgC, "Lead Consultant", "Senior consultant role in orgC");

        // one TENANT_ADMIN user per tenant, all sharing the
        // same dev password as requested. Emails are deterministic so this
        // stays idempotent-friendly and easy to find in Postman/DB.
        createTenantAdminUser(orgA, orgATenantAdminRole, "admin@orga.local", "OrgA Admin");
        createTenantAdminUser(orgB, orgBTenantAdminRole, "admin@orgb.local", "OrgB Admin");
        createTenantAdminUser(orgC, orgCTenantAdminRole, "admin@orgc.local", "OrgC Admin");
    }

    // now returns the saved role instead of void,
    // so the caller can assign a user to it afterward
    private RolesEntity createTenantAdminRole(
            TenantEntity tenant,
            List<PermissionEntity> tenantAdminPermissions
    ) {
        RolesEntity tenantAdminRole = new RolesEntity();
        tenantAdminRole.setRoleName("TENANT_ADMIN");
        tenantAdminRole.setDescription("Tenant administrator for " + tenant.getTenantName());
        tenantAdminRole.setScopeType(ScopeType.TENANT);
        tenantAdminRole.setTenant(tenant);

        for (PermissionEntity permission : tenantAdminPermissions) {
            RolePermissionEntity rolePermission = new RolePermissionEntity();
            rolePermission.setRole(tenantAdminRole);
            rolePermission.setPermission(permission);
            tenantAdminRole.getRolePermission().add(rolePermission);
        }

        return rolesRepo.save(tenantAdminRole);
    }

    private void createUnprivilegedTenantRole(
            TenantEntity tenant,
            String roleName,
            String description
    ) {
        RolesEntity role = new RolesEntity();
        role.setRoleName(roleName);
        role.setDescription(description);
        role.setScopeType(ScopeType.TENANT);
        role.setTenant(tenant);

        rolesRepo.save(role);
    }

    //  creates a tenant-scoped user and assigns them the given
    // tenant's TENANT_ADMIN role. Mirrors the SuperAdmin creation pattern
    // above — same shape, just tenant-bound instead of platform-bound.
    private void createTenantAdminUser(
            TenantEntity tenant,
            RolesEntity tenantAdminRole,
            String email,
            String name
    ) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("secret123456"));
        user.setTenant(tenant);

        user = userRepo.save(user);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(tenantAdminRole);
        userRole.setTenant(tenant);

        userRoleRepo.save(userRole);
    }
}
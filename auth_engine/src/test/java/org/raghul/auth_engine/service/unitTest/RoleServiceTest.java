package org.raghul.auth_engine.service.unitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raghul.auth_engine.dto.AuditLogRequest;
import org.raghul.auth_engine.dto.RegisterRoleRequest;
import org.raghul.auth_engine.dto.SetPermissionRequest;
import org.raghul.auth_engine.entity.PermissionApplicability;
import org.raghul.auth_engine.entity.PermissionEntity;
import org.raghul.auth_engine.entity.RolePermissionEntity;
import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.ScopeType;
import org.raghul.auth_engine.entity.TenantEntity;
import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.PermissionRepo;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.TenantRepo;
import org.raghul.auth_engine.service.AuditLogService;
import org.raghul.auth_engine.service.CurrentUserService;
import org.raghul.auth_engine.service.RoleService;
import org.raghul.auth_engine.service.TenantAccessPolicy;
import org.springframework.security.access.AccessDeniedException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    private static final Integer ACTOR_USER_ID = 1;
    private static final Integer ACTOR_TENANT_ID = 10;
    private static final Integer TARGET_TENANT_ID = 10;
    private static final Integer ROLE_ID = 20;
    private static final String USER_READ = "USER_READ";
    private static final String USER_CREATE = "USER_CREATE";
    private static final String PLATFORM_AUDIT_VIEW = "PLATFORM_AUDIT_VIEW";

    @Mock private RolesRepo rolesRepo;
    @Mock private TenantRepo tenantRepo;
    @Mock private PermissionRepo permissionRepo;
    @Mock private TenantAccessPolicy tenantAccessPolicy;
    @Mock private AuditLogService auditLogService;
    @Mock private CurrentUserService currentUserService;
    @InjectMocks private RoleService roleService;
    @Captor private ArgumentCaptor<RolesEntity> roleCaptor;
    @Captor private ArgumentCaptor<AuditLogRequest> auditLogCaptor;private TenantEntity tenant;private CurrentUserService.CurrentActor actor;

    @BeforeEach
    void setUp() {
        tenant = new TenantEntity();
        tenant.setTenantId(TARGET_TENANT_ID);

        actor = new CurrentUserService.CurrentActor(ACTOR_USER_ID, ACTOR_TENANT_ID);
    }

    @Test
    void createNewRole_whenTenantScopeAndAuthorized_savesTenantRole() {
        RegisterRoleRequest request = new RegisterRoleRequest("TENANT_MANAGER",
                "Can manage tenant users", TARGET_TENANT_ID, ScopeType.TENANT);
        RolesEntity savedRole = buildRole(ROLE_ID, "TENANT_MANAGER", ScopeType.TENANT, tenant);

        when(tenantRepo.findById(TARGET_TENANT_ID)).thenReturn(Optional.of(tenant));
        when(rolesRepo.save(any(RolesEntity.class))).thenReturn(savedRole);

        RolesEntity result = roleService.createNewRole(request);
        assertSame(savedRole, result);

        verify(tenantAccessPolicy).verifyCanAccessTenant(TARGET_TENANT_ID);
        verify(tenantAccessPolicy, never()).verifyCanAccessPlatform();
        assertTenantRoleSavedCorrectly(request);
    }

    @Test
    void createNewRole_whenTenantScopeHasNullTenantId_throwsIllegalArgumentException() {
        RegisterRoleRequest request = new RegisterRoleRequest("TENANT_MANAGER",
                "Can manage tenant users", null, ScopeType.TENANT);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roleService.createNewRole(request)
        );

        assertEquals("Tenant ID is required for a tenant-scoped role", exception.getMessage());

        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(tenantAccessPolicy, never()).verifyCanAccessPlatform();
        verify(tenantRepo, never()).findById(any());
        verify(rolesRepo, never()).save(any(RolesEntity.class));
    }

    @Test
    void createNewRole_whenTenantDoesNotExist_throwsResourceNotFoundAndDoesNotSaveRole() {
        RegisterRoleRequest request = new RegisterRoleRequest("TENANT_MANAGER",
                "Can manage tenant users", TARGET_TENANT_ID, ScopeType.TENANT);

        when(tenantRepo.findById(TARGET_TENANT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.createNewRole(request));

        assertEquals("Invalid tenant id: " + TARGET_TENANT_ID, exception.getMessage());

        verify(tenantAccessPolicy).verifyCanAccessTenant(TARGET_TENANT_ID);
        verify(rolesRepo, never()).save(any(RolesEntity.class));
    }

    @Test
    void createNewRole_whenPlatformScopeAndAuthorized_savesPlatformRoleWithoutTenant() {
        RegisterRoleRequest request = new RegisterRoleRequest("PLATFORM_ADMIN",
                "Can manage platform resources", null, ScopeType.PLATFORM);

        RolesEntity savedRole = buildRole(ROLE_ID, "PLATFORM_ADMIN", ScopeType.PLATFORM, null);

        when(rolesRepo.save(any(RolesEntity.class))).thenReturn(savedRole);

        RolesEntity result = roleService.createNewRole(request);
        assertSame(savedRole, result);

        verify(tenantAccessPolicy).verifyCanAccessPlatform();
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(tenantRepo, never()).findById(any());
        assertPlatformRoleSavedCorrectly(request);
    }

    @Test
    void createNewRole_whenScopeIsNull_throwsIllegalArgumentExceptionAndDoesNotSaveRole() {
        RegisterRoleRequest request = new RegisterRoleRequest("INVALID_ROLE",
                "Role with an invalid scope", TARGET_TENANT_ID, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roleService.createNewRole(request));

        assertEquals("Invalid role scope", exception.getMessage());

        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(tenantAccessPolicy, never()).verifyCanAccessPlatform();
        verify(tenantRepo, never()).findById(any());
        verify(rolesRepo, never()).save(any(RolesEntity.class));
    }



    @Test
    void setNewPermissionsToRole_whenTenantRoleAndPermissionsAreValid_assignsPermissionsAndCreatesAllowAudits() {
        RolesEntity tenantRole = buildRole(ROLE_ID, "TENANT_MANAGER", ScopeType.TENANT, tenant);

        PermissionEntity userRead = buildPermission(USER_READ, PermissionApplicability.TENANT_ONLY);
        PermissionEntity userCreate = buildPermission(USER_CREATE, PermissionApplicability.TENANT_ONLY);
        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(USER_READ, USER_CREATE));

        stubAuthorizedTenantPermissionAssignment(tenantRole, request, List.of(userRead, userCreate));

        List<RolePermissionEntity> result = roleService.setNewPermissionsToRole(request);

        assertEquals(2, result.size());
        assertEquals(2, tenantRole.getRolePermission().size());
        assertRolePermission(result.get(0), tenantRole, userRead);
        assertRolePermission(result.get(1), tenantRole, userCreate);
        verify(tenantAccessPolicy).verifyCanAccessTenant(TARGET_TENANT_ID);
        verify(tenantAccessPolicy, never()).verifyCanAccessPlatform();
        verify(rolesRepo).save(tenantRole);
        assertRolePermissionAllowAudits(List.of(userRead, userCreate), TARGET_TENANT_ID);
    }

    @Test
    void setNewPermissionsToRole_whenRoleDoesNotExist_throwsResourceNotFoundException() {
        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(USER_READ));

        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> roleService.setNewPermissionsToRole(request));

        assertEquals("Role not found with id: " + ROLE_ID, exception.getMessage());

        verify(currentUserService, never()).getCurrentActor();
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(tenantAccessPolicy, never()).verifyCanAccessPlatform();
        verify(permissionRepo, never()).findByPermissionNameIn(any());
        verify(rolesRepo, never()).save(any(RolesEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    @Test
    void setNewPermissionsToRole_whenTenantAuthorizationDenies_logsDenyAuditAndDoesNotAssignPermissions() {
        RolesEntity tenantRole = buildRole(ROLE_ID, "TENANT_MANAGER", ScopeType.TENANT, tenant);
        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(USER_READ));
        AccessDeniedException deniedException = new AccessDeniedException("Cannot access tenant " + TARGET_TENANT_ID);

        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.of(tenantRole));
        when(currentUserService.getCurrentActor()).thenReturn(actor);

        doThrow(deniedException).when(tenantAccessPolicy).verifyCanAccessTenant(TARGET_TENANT_ID);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> roleService.setNewPermissionsToRole(request));

        assertSame(deniedException, exception);

        verify(permissionRepo, never()).findByPermissionNameIn(any());
        verify(rolesRepo, never()).save(any(RolesEntity.class));
        assertRolePermissionDenyAudit(ROLE_ID, TARGET_TENANT_ID, deniedException.getMessage());
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

    @Test
    void setNewPermissionsToRole_whenPlatformRoleAndPermissionAreValid_assignsPermissionUsingPlatformAuthorization() {
        RolesEntity platformRole = buildRole(ROLE_ID, "PLATFORM_ADMIN", ScopeType.PLATFORM, null);
        PermissionEntity platformAuditView = buildPermission(PLATFORM_AUDIT_VIEW, PermissionApplicability.PLATFORM_ONLY);
        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(PLATFORM_AUDIT_VIEW));

        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.of(platformRole));
        when(currentUserService.getCurrentActor()).thenReturn(actor);
        when(permissionRepo.findByPermissionNameIn(request.permissionSet())).thenReturn(List.of(platformAuditView));

        List<RolePermissionEntity> result = roleService.setNewPermissionsToRole(request);
        assertEquals(1, result.size());
        assertRolePermission(result.get(0), platformRole, platformAuditView);

        verify(tenantAccessPolicy).verifyCanAccessPlatform();
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(rolesRepo).save(platformRole);
        assertRolePermissionAllowAudits(List.of(platformAuditView), null);
    }

    @Test
    void setNewPermissionsToRole_whenRequestedPermissionIsMissing_throwsResourceNotFoundAndDoesNotSaveRole() {
        RolesEntity tenantRole = buildRole(ROLE_ID, "TENANT_MANAGER", ScopeType.TENANT, tenant);
        PermissionEntity userRead = buildPermission(USER_READ, PermissionApplicability.TENANT_ONLY);
        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(USER_READ, "MISSING_PERMISSION"));

        stubAuthorizedTenantPermissionAssignment(tenantRole, request, List.of(userRead));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> roleService.setNewPermissionsToRole(request));

        assertTrue(exception.getMessage().contains("Permissions not found"));
        assertTrue(exception.getMessage().contains("MISSING_PERMISSION"));

        verify(rolesRepo, never()).save(any(RolesEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

    @Test
    void setNewPermissionsToRole_whenPlatformOnlyPermissionIsAssignedToTenantRole_throwsIllegalArgumentException() {
        RolesEntity tenantRole = buildRole(ROLE_ID, "TENANT_MANAGER", ScopeType.TENANT, tenant);
        PermissionEntity platformAuditView = buildPermission(PLATFORM_AUDIT_VIEW, PermissionApplicability.PLATFORM_ONLY);
        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(PLATFORM_AUDIT_VIEW));

        stubAuthorizedTenantPermissionAssignment(tenantRole, request, List.of(platformAuditView));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> roleService.setNewPermissionsToRole(request));

        assertEquals("Platform-only permission cannot be assigned to a tenant role: "
                        + PLATFORM_AUDIT_VIEW, exception.getMessage());

        verify(rolesRepo, never()).save(any(RolesEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

    @Test
    void setNewPermissionsToRole_whenTenantOnlyPermissionIsAssignedToPlatformRole_throwsIllegalArgumentException() {
        RolesEntity platformRole = buildRole(ROLE_ID, "PLATFORM_ADMIN", ScopeType.PLATFORM, null);
        PermissionEntity userRead = buildPermission(USER_READ, PermissionApplicability.TENANT_ONLY);
        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(USER_READ));

        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.of(platformRole));
        when(currentUserService.getCurrentActor()).thenReturn(actor);
        when(permissionRepo.findByPermissionNameIn(request.permissionSet())).thenReturn(List.of(userRead));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> roleService.setNewPermissionsToRole(request));
        assertEquals("Tenant-only permission cannot be assigned to a platform role: " + USER_READ, exception.getMessage());

        verify(tenantAccessPolicy).verifyCanAccessPlatform();
        verify(rolesRepo, never()).save(any(RolesEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

    @Test
    void setNewPermissionsToRole_whenPermissionAlreadyAssigned_doesNotCreateDuplicateAssignmentOrAudit() {
        RolesEntity tenantRole = buildRole(ROLE_ID, "TENANT_MANAGER", ScopeType.TENANT, tenant);
        PermissionEntity userRead = buildPermission(USER_READ, PermissionApplicability.TENANT_ONLY);
        RolePermissionEntity existingAssignment = new RolePermissionEntity();

        existingAssignment.setRole(tenantRole);
        existingAssignment.setPermission(userRead);
        tenantRole.getRolePermission().add(existingAssignment);

        SetPermissionRequest request = new SetPermissionRequest(ROLE_ID, Set.of(USER_READ));

        stubAuthorizedTenantPermissionAssignment(tenantRole, request, List.of(userRead));

        List<RolePermissionEntity> result = roleService.setNewPermissionsToRole(request);

        assertTrue(result.isEmpty());
        assertEquals(1, tenantRole.getRolePermission().size());
        verify(rolesRepo).save(tenantRole);
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }



    private RolesEntity buildRole(Integer roleId, String roleName, ScopeType scope, TenantEntity roleTenant) {
        RolesEntity role = new RolesEntity();

        role.setRoleId(roleId);
        role.setRoleName(roleName);
        role.setScopeType(scope);
        role.setTenant(roleTenant);
        role.setRolePermission(new HashSet<>());
        return role;
    }

    private PermissionEntity buildPermission(String permissionName, PermissionApplicability applicability) {
        PermissionEntity permission = new PermissionEntity();
        permission.setPermissionName(permissionName);
        permission.setApplicability(applicability);
        return permission;
    }

    private void stubAuthorizedTenantPermissionAssignment(RolesEntity tenantRole, SetPermissionRequest request, List<PermissionEntity> foundPermissions) {
        when(rolesRepo.findById(request.roleId())).thenReturn(Optional.of(tenantRole));
        when(currentUserService.getCurrentActor()).thenReturn(actor);
        when(permissionRepo.findByPermissionNameIn(request.permissionSet())).thenReturn(foundPermissions);
    }

    private void assertTenantRoleSavedCorrectly(RegisterRoleRequest request) {
        verify(rolesRepo).save(roleCaptor.capture());
        RolesEntity roleToSave = roleCaptor.getValue();

        assertEquals(request.roleName(), roleToSave.getRoleName());
        assertEquals(request.description(), roleToSave.getDescription());
        assertEquals(ScopeType.TENANT, roleToSave.getScopeType());
        assertSame(tenant, roleToSave.getTenant());
    }

    private void assertPlatformRoleSavedCorrectly(RegisterRoleRequest request) {
        verify(rolesRepo).save(roleCaptor.capture());
        RolesEntity roleToSave = roleCaptor.getValue();

        assertEquals(request.roleName(), roleToSave.getRoleName());
        assertEquals(request.description(), roleToSave.getDescription());
        assertEquals(ScopeType.PLATFORM, roleToSave.getScopeType());
        assertNull(roleToSave.getTenant());
    }

    private void assertRolePermission(RolePermissionEntity assignment, RolesEntity expectedRole, PermissionEntity expectedPermission) {
        assertSame(expectedRole, assignment.getRole());
        assertSame(expectedPermission, assignment.getPermission());
    }

    private void assertRolePermissionAllowAudits(List<PermissionEntity> assignedPermissions, Integer expectedTargetTenantId) {
        verify(auditLogService, times(assignedPermissions.size())).log(auditLogCaptor.capture());
        List<AuditLogRequest> auditRequests = auditLogCaptor.getAllValues();
        assertEquals(assignedPermissions.size(), auditRequests.size());
        for (int index = 0; index < assignedPermissions.size(); index++) {
            PermissionEntity permission = assignedPermissions.get(index);
            AuditLogRequest auditRequest = auditRequests.get(index);

            assertEquals(ACTOR_USER_ID, auditRequest.actorUserId());
            assertEquals(ACTOR_TENANT_ID, auditRequest.actorTenantId());
            assertEquals(AuditAction.ROLE_PERMISSION_ASSIGN, auditRequest.action());
            assertEquals("ROLE", auditRequest.targetResourceType());
            assertEquals(ROLE_ID, auditRequest.targetResourceId());
            assertEquals(expectedTargetTenantId, auditRequest.targetTenantId());
            assertEquals(AuditDecision.ALLOW, auditRequest.decision());
            assertEquals("Permission assigned successfully: permissionId=" + permission.getPermissionId() + ", permissionName=" + permission.getPermissionName(), auditRequest.reason());
        }
    }

    private void assertRolePermissionDenyAudit(Integer expectedRoleId, Integer expectedTargetTenantId, String expectedReason
    ) {
        verify(auditLogService).logDenied(auditLogCaptor.capture());
        AuditLogRequest auditRequest = auditLogCaptor.getValue();

        assertEquals(ACTOR_USER_ID, auditRequest.actorUserId());
        assertEquals(ACTOR_TENANT_ID, auditRequest.actorTenantId());
        assertEquals(AuditAction.ROLE_PERMISSION_ASSIGN, auditRequest.action());
        assertEquals("ROLE", auditRequest.targetResourceType());
        assertEquals(expectedRoleId, auditRequest.targetResourceId());
        assertEquals(expectedTargetTenantId, auditRequest.targetTenantId());
        assertEquals(AuditDecision.DENY, auditRequest.decision());
        assertEquals(expectedReason, auditRequest.reason());
    }
}
package org.raghul.auth_engine.service.unitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raghul.auth_engine.dto.AssignRoleRequest;
import org.raghul.auth_engine.dto.AuditLogRequest;
import org.raghul.auth_engine.entity.*;
import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.repository.UserRoleRepo;
import org.raghul.auth_engine.service.AuditLogService;
import org.raghul.auth_engine.service.CurrentUserService;
import org.raghul.auth_engine.service.TenantAccessPolicy;
import org.raghul.auth_engine.service.UserRoleService;
import org.springframework.security.access.AccessDeniedException;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserRoleServiceTest {

    private TenantAccessPolicy tenantAccessPolicy;
    private UserRoleRepo userRoleRepo;
    private UserRepo userRepo;
    private RolesRepo rolesRepo;
    private AuditLogService auditLogService;
    private CurrentUserService currentUserService;
    private UserRoleService userRoleService;


    @BeforeEach
    void setUp(){
        tenantAccessPolicy = mock(TenantAccessPolicy.class);
        userRoleRepo = mock(UserRoleRepo.class);
        userRepo = mock(UserRepo.class);
        rolesRepo = mock(RolesRepo.class);
        auditLogService = mock(AuditLogService.class);
        currentUserService = mock(CurrentUserService.class);

        userRoleService = new UserRoleService(
                tenantAccessPolicy,
                userRoleRepo,
                userRepo,
                rolesRepo,
                auditLogService,
                currentUserService
        );
    }

    /**
     * Test wether user.tenantId  == role.tenantID, if not the test fails
     * **/
    @Test
    void assignRoleToUser_whenUserAndRoleBelongToSameTenant_saveAssignment(){
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);

        TenantEntity tenant2 = new TenantEntity();
        tenant2.setTenantId(2);

        UserEntity user = new UserEntity();
        user.setuId(10);
        user.setTenant(tenant);

        RolesEntity role = new RolesEntity();
        role.setRoleId(5);
        role.setRoleName("TENANT_ADMIN");
        role.setScopeType(ScopeType.TENANT);
        role.setTenant(tenant);


        when(userRoleRepo.existsByUserAndRoleAndTenant(user,role,tenant)).thenReturn(false);

        UserRoleEntity savedAssignment = new UserRoleEntity();
        savedAssignment.setUser(user);
        savedAssignment.setRole(role);
        savedAssignment.setTenant(tenant);

        when(userRoleRepo.save(any(UserRoleEntity.class)))
                .thenReturn(savedAssignment);

        when(currentUserService.getCurrentActor())
                .thenReturn(new CurrentUserService.CurrentActor(99, 1));

        UserRoleEntity result =
                userRoleService.assignRoleToUser(user, role, tenant);

        assertThat(result).isSameAs(savedAssignment);
    }

    @Test
    void assignRoleToUser_whenTenantRoleHasNoAssignmentTenant_throwsDoesNotSave(){


        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);
        UserEntity user = new UserEntity();
        user.setuId(10);
        user.setTenant(tenant);
        RolesEntity role = new RolesEntity();
        role.setTenant(tenant);
        role.setScopeType(ScopeType.TENANT);

        assertThatThrownBy(() ->
                userRoleService.assignRoleToUser(user, role, null)
        )
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
    }

    @Test
    void assignRoleToUser_whenUserRoleIsAlreadyPresent(){
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);
        UserEntity user = new UserEntity();
        user.setTenant(tenant);
        user.setuId(100);

        RolesEntity role = new RolesEntity();
        role.setTenant(tenant);
        role.setScopeType(ScopeType.TENANT);
        role.setRoleId(99);
        when(userRoleRepo.existsByUserAndRoleAndTenant(user, role, tenant)).thenReturn(true);
        assertThatThrownBy(() -> userRoleService.assignRoleToUser(user, role, tenant))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    void assignRoleToUser_whenPlatformRoleHasAssignmentTenant_throwsDoesNotSave(){
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);

        UserEntity user = new UserEntity();
        user.setTenant(tenant);

        RolesEntity role = new RolesEntity();
        role.setScopeType(ScopeType.PLATFORM);
        role.setRoleId(100);

        assertThatThrownBy(() -> userRoleService.assignRoleToUser(user, role, tenant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assigning Platform role to a tenant is not allowed");

        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
    }

    @Test
    void assignRoleToUser_whenTenantUserReceivesPlatformRole_throwsDoesNotSave(){
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);

        UserEntity user = new UserEntity();
        user.setTenant(tenant);

        RolesEntity role = new RolesEntity();
        role.setScopeType(ScopeType.PLATFORM);
        role.setRoleId(100);

        assertThatThrownBy(() -> userRoleService.assignRoleToUser(user, role, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Assigning a Platform role to a tenant user is not allowed");

        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
    }

    @Test
    void assignRoleToUser_whenUserTenantDiffersFromAssignmentTenant_throwsDoesNotSave() {


        TenantEntity tenantOne = new TenantEntity();
        tenantOne.setTenantId(1);

        TenantEntity tenantTwo = new TenantEntity();
        tenantTwo.setTenantId(2);

        UserEntity user = new UserEntity();
        user.setuId(10);
        user.setTenant(tenantOne);

        RolesEntity role = new RolesEntity();
        role.setRoleId(5);
        role.setRoleName("TENANT_ADMIN");
        role.setScopeType(ScopeType.TENANT);
        role.setTenant(tenantOne);

        assertThatThrownBy(() ->
                userRoleService.assignRoleToUser(user, role, tenantTwo)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User tenant mismatch");

        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
    }

    @Test
    void assignRoleToUser_whenRoleTenantDiffersFromAssignmentTenant_throwsDoesNotSave() {


        TenantEntity tenantOne = new TenantEntity();
        tenantOne.setTenantId(1);

        TenantEntity tenantTwo = new TenantEntity();
        tenantTwo.setTenantId(2);

        UserEntity user = new UserEntity();
        user.setuId(10);
        user.setTenant(tenantOne);

        RolesEntity role = new RolesEntity();
        role.setRoleId(5);
        role.setRoleName("TENANT_ADMIN");
        role.setScopeType(ScopeType.TENANT);
        role.setTenant(tenantTwo);


        assertThatThrownBy(() ->
                userRoleService.assignRoleToUser(user, role, tenantOne)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role tenant mismatch");

        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
    }

    @Test
    void assignRoleToUser_whenPlatformUserReceivesPlatformRole_savesAndCreatesAllowAudit() {

        UserEntity platformUser = new UserEntity();
        platformUser.setuId(10);
        platformUser.setTenant(null);

        RolesEntity platformRole = new RolesEntity();
        platformRole.setRoleId(100);
        platformRole.setRoleName("SUPER_ADMIN");
        platformRole.setScopeType(ScopeType.PLATFORM);
        platformRole.setTenant(null);

        UserRoleEntity savedAssignment = new UserRoleEntity();
        savedAssignment.setUser(platformUser);
        savedAssignment.setRole(platformRole);
        savedAssignment.setTenant(null);

        when(userRoleRepo.existsByUserAndRoleAndTenant(platformUser, platformRole, null)).thenReturn(false);

        when(userRoleRepo.save(any(UserRoleEntity.class)))
                .thenReturn(savedAssignment);

        when(currentUserService.getCurrentActor()).thenReturn(new CurrentUserService.CurrentActor(99, null));


        UserRoleEntity result = userRoleService.assignRoleToUser(platformUser, platformRole, null);

        assertThat(result).isSameAs(savedAssignment);

        verify(userRoleRepo).save(argThat(assignment ->
                assignment.getUser() == platformUser
                        && assignment.getRole() == platformRole
                        && assignment.getTenant() == null
        ));

        verify(auditLogService).log(argThat(audit ->
                audit.action() == AuditAction.USER_ROLE_ASSIGN
                        && audit.decision() == AuditDecision.ALLOW
                        && audit.targetResourceType().equals("USER")
                        && audit.targetResourceId().equals(10)
                        && audit.targetTenantId() == null
        ));
    }

    @Test
    void assignRoleToUser_whenPlatformUserReceivesTenantRole_throwsDoesNotSave() {

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);

        UserEntity platformUser = new UserEntity();
        platformUser.setuId(10);
        platformUser.setTenant(null);

        RolesEntity tenantRole = new RolesEntity();
        tenantRole.setRoleId(5);
        tenantRole.setRoleName("TENANT_ADMIN");
        tenantRole.setScopeType(ScopeType.TENANT);
        tenantRole.setTenant(tenant);

        assertThatThrownBy(() -> userRoleService
                .assignRoleToUser(platformUser, tenantRole, tenant))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tenant user must belong to a tenant");

        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
    }

    @Test
    void addRoleToExistingUser_whenActorCannotAccessTargetTenant_logsDenyAndThrows() {

        // Arrange
        TenantEntity targetTenant = new TenantEntity();
        targetTenant.setTenantId(2);

        UserEntity targetUser = new UserEntity();
        targetUser.setuId(10);
        targetUser.setTenant(targetTenant);

        AssignRoleRequest request = new AssignRoleRequest(10, 5, 2);

        when(userRepo.findById(10)).thenReturn(Optional.of(targetUser));

        when(currentUserService.getCurrentActor()).thenReturn(new CurrentUserService.CurrentActor(99, 1));

        doThrow(new AccessDeniedException("Cross-tenant access denied"))
                .when(tenantAccessPolicy).verifyCanAccessTenant(2);

        assertThatThrownBy(() ->
                userRoleService.addRoleToExistingUser(request)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Cross-tenant access denied");

        verify(rolesRepo, never()).findById(any());
        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));

        verify(auditLogService).logDenied(argThat(audit ->
                audit.action() == AuditAction.USER_ROLE_ASSIGN
                        && audit.decision() == AuditDecision.DENY
                        && audit.targetResourceType().equals("USER")
                        && audit.targetResourceId().equals(10)
                        && audit.targetTenantId().equals(2)
        ));
    }

    @Test
    void assignRoleToUser_whenAssignmentAlreadyExists_throwsDoesNotSaveOrCreateAllowAudit() {

        // Arrange
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);

        UserEntity user = new UserEntity();
        user.setTenant(tenant);
        user.setuId(100);

        RolesEntity role = new RolesEntity();
        role.setTenant(tenant);
        role.setScopeType(ScopeType.TENANT);
        role.setRoleId(99);

        when(userRoleRepo.existsByUserAndRoleAndTenant(user, role, tenant))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() ->
                userRoleService.assignRoleToUser(user, role, tenant)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("This role is already assigned to the user");

        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

    @Test
    void addRoleToExistingUser_whenUserDoesNotExist_throwsAndDoesNotContinue() {

        // Arrange
        AssignRoleRequest request = new AssignRoleRequest(10, 5, 2);

        when(userRepo.findById(10))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() ->
                userRoleService.addRoleToExistingUser(request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 10");

        verifyNoInteractions(tenantAccessPolicy);
        verifyNoInteractions(rolesRepo);
        verifyNoInteractions(userRoleRepo);
        verifyNoInteractions(auditLogService);
    }

    @Test
    void addRoleToExistingUser_whenRoleDoesNotExist_throwsAndDoesNotSaveOrCreateAllowAudit() {

        // Arrange
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(1);

        UserEntity user = new UserEntity();
        user.setuId(10);
        user.setTenant(tenant);

        AssignRoleRequest request = new AssignRoleRequest(10, 5, 1);

        when(userRepo.findById(10)).thenReturn(Optional.of(user));
        when(currentUserService.getCurrentActor()).thenReturn(new CurrentUserService.CurrentActor(99, 1));
        when(rolesRepo.findById(5)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.addRoleToExistingUser(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Role not found with id: 5");

        verify(tenantAccessPolicy).verifyCanAccessTenant(1);
        verify(userRoleRepo, never()).save(any(UserRoleEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

}

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
import org.raghul.auth_engine.dto.DeleteUserRequest;
import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.TenantEntity;
import org.raghul.auth_engine.entity.UserEntity;
import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.TenantRepo;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.service.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final Integer ACTOR_USER_ID = 1;
    private static final Integer TENANT_ID = 10;
    private static final Integer ROLE_ID = 20;
    private static final Integer SAVED_USER_ID = 100;
    private static final String USER_NAME = "Raghul";
    private static final String USER_EMAIL = "raghul@example.com";
    private static final String RAW_PASSWORD = "raw-password";
    private static final String ENCODED_PASSWORD = "encoded-password";
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private UserRepo userRepo;
    @Mock private RolesRepo rolesRepo;
    @Mock private TenantRepo tenantRepo;
    @Mock private TenantAccessPolicy tenantAccessPolicy;
    @Mock private UserRoleService userRoleService;
    @Mock private AuditLogService auditLogService;
    @Mock private CurrentUserService currentUserService;
    @InjectMocks private UserService userService;
    @Captor private ArgumentCaptor<UserEntity> userCaptor;
    @Captor private ArgumentCaptor<AuditLogRequest> auditLogCaptor;

    private RegisterUserRequest request;
    private RolesEntity role;
    private TenantEntity tenant;
    private CurrentUserService.CurrentActor actor;

    @BeforeEach
    void setUp() {
        request = new RegisterUserRequest(USER_NAME, USER_EMAIL, RAW_PASSWORD, TENANT_ID, ROLE_ID);

        tenant = new TenantEntity();
        tenant.setTenantId(TENANT_ID);

        role = new RolesEntity();
        role.setRoleId(ROLE_ID);
        role.setRoleName("TENANT_ADMIN");

        actor = new CurrentUserService.CurrentActor(ACTOR_USER_ID, TENANT_ID);
    }

    @Test
    void registerUser_whenValidTenantUserRequest_savesUserAssignsInitialRoleAndCreatesAllowAudit() {
        UserEntity savedUser = buildSavedUser();
        stubValidRegistration(savedUser);

        UserEntity result = userService.registerUser(request);
        assertSame(savedUser, result);

        verify(tenantAccessPolicy).verifyCanAccessTenant(TENANT_ID);
        assertUserSavedCorrectly(ENCODED_PASSWORD);
        verify(userRoleService).assignRoleToUser(savedUser, role, tenant);
        assertUserCreateAllowAudit(savedUser);
    }

    @Test
    void registerUser_whenRoleDoesNotExist_throwsResourceNotFoundAndDoesNotSaveUser() {
        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.registerUser(request));

        assertEquals("Invalid role id: " + ROLE_ID, exception.getMessage());

        verify(tenantRepo, never()).findById(any());
        verify(currentUserService, never()).getCurrentActor();
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(userRepo, never()).existsByTenantAndEmail(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepo, never()).save(any(UserEntity.class));
        verify(userRoleService, never()).assignRoleToUser(any(), any(), any());
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    @Test
    void registerUser_whenTenantDoesNotExist_throwsResourceNotFoundAndDoesNotSaveUser() {
        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.of(role));
        when(tenantRepo.findById(TENANT_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.registerUser(request));

        assertEquals("Invalid tenant id: " + TENANT_ID, exception.getMessage());

        verify(currentUserService, never()).getCurrentActor();
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(userRepo, never()).existsByTenantAndEmail(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepo, never()).save(any(UserEntity.class));
        verify(userRoleService, never()).assignRoleToUser(any(), any(), any());
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    @Test
    void registerUser_whenActorCannotAccessTargetTenant_logsDenyAndThrowsWithoutSavingUser() {
        AccessDeniedException deniedException = new AccessDeniedException("Cannot access tenant " + TENANT_ID);

        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.of(role));
        when(tenantRepo.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(currentUserService.getCurrentActor()).thenReturn(actor);

        doThrow(deniedException).when(tenantAccessPolicy).verifyCanAccessTenant(TENANT_ID);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> userService.registerUser(request));
        assertSame(deniedException, exception);
        assertUserCreateDenyAudit(deniedException.getMessage());

        verify(userRepo, never()).existsByTenantAndEmail(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepo, never()).save(any(UserEntity.class));
        verify(userRoleService, never()).assignRoleToUser(any(), any(), any());
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

    @Test
    void registerUser_whenEmailAlreadyExistsInSameTenant_throwsAndDoesNotSaveUser() {
        stubExistingRoleTenantAndActor();
        when(userRepo.existsByTenantAndEmail(tenant, USER_EMAIL)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.registerUser(request));

        assertEquals("Email already exists in this tenant", exception.getMessage());

        verify(userRepo).existsByTenantAndEmail(tenant, USER_EMAIL);
        verify(passwordEncoder, never()).encode(any());
        verify(userRepo, never()).save(any(UserEntity.class));
        verify(userRoleService, never()).assignRoleToUser(any(), any(), any());
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    @Test
    void registerUser_whenInitialRoleAssignmentFails_propagatesExceptionAndDoesNotCreateUserAllowAudit() {
        UserEntity savedUser = buildSavedUser();
        RuntimeException assignmentException = new RuntimeException("Role assignment failed");

        stubValidRegistration(savedUser);

        doThrow(assignmentException).when(userRoleService).assignRoleToUser(savedUser, role, tenant);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(request));

        assertSame(assignmentException, exception);

        verify(userRepo).save(any(UserEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }


    @Test
    void deleteUser_whenUserDoesNotExist_throwsResourceNotFoundAndDoesNotDelete() {
        DeleteUserRequest request = new DeleteUserRequest(100);

        when(userRepo.findById(100)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(request));
        assertEquals("User not found with id: 100", exception.getMessage());

        verify(currentUserService, never()).getCurrentActor();
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(tenantAccessPolicy, never()).verifyCanAccessPlatform();
        verify(userRepo, never()).delete(any(UserEntity.class));
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    @Test
    void deleteUser_whenTargetUserBelongsToAuthorizedTenant_deletesUserAndCreatesAllowAudit() {
        DeleteUserRequest request = new DeleteUserRequest(100);
        UserEntity targetUser = buildTenantUser(100, tenant);

        when(userRepo.findById(100)).thenReturn(Optional.of(targetUser));
        when(currentUserService.getCurrentActor()).thenReturn(actor);

        UserEntity result = userService.deleteUser(request);
        assertSame(targetUser, result);

        verify(tenantAccessPolicy).verifyCanAccessTenant(TENANT_ID);
        verify(userRepo).delete(targetUser);
        assertUserDeleteAllowAudit(targetUser, TENANT_ID);
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    @Test
    void deleteUser_whenTargetUserIsPlatformUserAndActorHasPlatformAccess_deletesUserAndCreatesAllowAudit() {
        DeleteUserRequest request = new DeleteUserRequest(100);
        UserEntity targetUser = buildPlatformUser(100);

        when(userRepo.findById(100)).thenReturn(Optional.of(targetUser));
        when(currentUserService.getCurrentActor()).thenReturn(actor);

        UserEntity result = userService.deleteUser(request);
        assertSame(targetUser, result);

        verify(tenantAccessPolicy).verifyCanAccessPlatform();
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(userRepo).delete(targetUser);
        assertUserDeleteAllowAudit(targetUser, null);
        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    @Test
    void deleteUser_whenActorCannotAccessTargetTenant_logsDenyAndDoesNotDeleteUser() {
        DeleteUserRequest request = new DeleteUserRequest(100);
        UserEntity targetUser = buildTenantUser(100, tenant);
        AccessDeniedException deniedException = new AccessDeniedException("Cannot access tenant " + TENANT_ID);

        when(userRepo.findById(100)).thenReturn(Optional.of(targetUser));
        when(currentUserService.getCurrentActor()).thenReturn(actor);

        doThrow(deniedException).when(tenantAccessPolicy).verifyCanAccessTenant(TENANT_ID);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> userService.deleteUser(request)
        );

        assertSame(deniedException, exception);
        verify(userRepo, never()).delete(any(UserEntity.class));
        assertUserDeleteDenyAudit(targetUser, TENANT_ID, deniedException.getMessage());
        verify(auditLogService, never())
                .log(any(AuditLogRequest.class));
    }

    @Test
    void deleteUser_whenActorCannotAccessPlatform_logsDenyAndDoesNotDeleteUser() {
        DeleteUserRequest request = new DeleteUserRequest(100);

        UserEntity targetUser = buildPlatformUser(100);
        AccessDeniedException deniedException = new AccessDeniedException("Platform access is required");

        when(userRepo.findById(100)).thenReturn(Optional.of(targetUser));
        when(currentUserService.getCurrentActor()).thenReturn(actor);

        doThrow(deniedException).when(tenantAccessPolicy).verifyCanAccessPlatform();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> userService.deleteUser(request)
        );

        assertSame(deniedException, exception);
        verify(tenantAccessPolicy, never()).verifyCanAccessTenant(any());
        verify(userRepo, never()).delete(any(UserEntity.class));
        assertUserDeleteDenyAudit(targetUser, null, deniedException.getMessage());
        verify(auditLogService, never()).log(any(AuditLogRequest.class));
    }

    /*
     * Stubs every dependency call required for a successful registerUser call.
     * Do not use this helper in tests that intentionally stop before save().
     */
    private void stubValidRegistration(UserEntity savedUser) {
        stubExistingRoleTenantAndActor();

        when(userRepo.existsByTenantAndEmail(tenant, USER_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepo.save(any(UserEntity.class))).thenReturn(savedUser);
    }

    private void stubExistingRoleTenantAndActor() {
        when(rolesRepo.findById(ROLE_ID)).thenReturn(Optional.of(role));
        when(tenantRepo.findById(TENANT_ID)).thenReturn(Optional.of(tenant));
        when(currentUserService.getCurrentActor()).thenReturn(actor);
    }

    private UserEntity buildSavedUser() {
        UserEntity savedUser = new UserEntity();

        savedUser.setuId(SAVED_USER_ID);
        savedUser.setName(USER_NAME);
        savedUser.setEmail(USER_EMAIL);
        savedUser.setPassword(ENCODED_PASSWORD);
        savedUser.setTenant(tenant);

        return savedUser;
    }

    private void assertUserSavedCorrectly(String expectedEncodedPassword) {
        verify(userRepo).save(userCaptor.capture());

        UserEntity userToSave = userCaptor.getValue();

        assertEquals(USER_NAME, userToSave.getName());
        assertEquals(USER_EMAIL, userToSave.getEmail());
        assertEquals(expectedEncodedPassword, userToSave.getPassword());
        assertNotEquals(RAW_PASSWORD, userToSave.getPassword());
        assertSame(tenant, userToSave.getTenant());
    }

    private void assertUserCreateAllowAudit(UserEntity savedUser) {
        verify(auditLogService).log(auditLogCaptor.capture());

        AuditLogRequest auditLogRequest = auditLogCaptor.getValue();

        assertEquals(ACTOR_USER_ID, auditLogRequest.actorUserId());
        assertEquals(TENANT_ID, auditLogRequest.actorTenantId());
        assertEquals(AuditAction.USER_CREATE, auditLogRequest.action());
        assertEquals("USER", auditLogRequest.targetResourceType());
        assertEquals(savedUser.getuId(), auditLogRequest.targetResourceId());
        assertEquals(TENANT_ID, auditLogRequest.targetTenantId());
        assertEquals(AuditDecision.ALLOW, auditLogRequest.decision());
        assertEquals("User created successfully", auditLogRequest.reason());

        verify(auditLogService, never()).logDenied(any(AuditLogRequest.class));
    }

    private void assertUserCreateDenyAudit(String expectedReason) {
        verify(auditLogService).logDenied(auditLogCaptor.capture());

        AuditLogRequest auditLogRequest = auditLogCaptor.getValue();

        assertEquals(ACTOR_USER_ID, auditLogRequest.actorUserId());
        assertEquals(TENANT_ID, auditLogRequest.actorTenantId());
        assertEquals(AuditAction.USER_CREATE, auditLogRequest.action());
        assertEquals("USER", auditLogRequest.targetResourceType());
        assertNull(auditLogRequest.targetResourceId());
        assertEquals(TENANT_ID, auditLogRequest.targetTenantId());
        assertEquals(AuditDecision.DENY, auditLogRequest.decision());
        assertEquals(expectedReason, auditLogRequest.reason());
    }


    private UserEntity buildTenantUser(Integer userId, TenantEntity userTenant) {
        UserEntity user = new UserEntity();

        user.setuId(userId);
        user.setName("Target User");
        user.setEmail("target@example.com");
        user.setTenant(userTenant);

        return user;
    }

    private UserEntity buildPlatformUser(Integer userId) {
        UserEntity user = new UserEntity();

        user.setuId(userId);
        user.setName("Platform User");
        user.setEmail("platform@example.com");
        user.setTenant(null);

        return user;
    }

    private void assertUserDeleteAllowAudit(UserEntity targetUser, Integer expectedTargetTenantId) {
        verify(auditLogService).log(auditLogCaptor.capture());

        AuditLogRequest auditLogRequest = auditLogCaptor.getValue();

        assertEquals(ACTOR_USER_ID, auditLogRequest.actorUserId());
        assertEquals(TENANT_ID, auditLogRequest.actorTenantId());
        assertEquals(AuditAction.USER_DELETE, auditLogRequest.action());
        assertEquals("USER", auditLogRequest.targetResourceType());
        assertEquals(targetUser.getuId(), auditLogRequest.targetResourceId());
        assertEquals(expectedTargetTenantId, auditLogRequest.targetTenantId());
        assertEquals(AuditDecision.ALLOW, auditLogRequest.decision());
        assertEquals("User deleted successfully", auditLogRequest.reason());
    }

    private void assertUserDeleteDenyAudit(UserEntity targetUser, Integer expectedTargetTenantId, String expectedReason) {
        verify(auditLogService).logDenied(auditLogCaptor.capture());

        AuditLogRequest auditLogRequest = auditLogCaptor.getValue();

        assertEquals(ACTOR_USER_ID, auditLogRequest.actorUserId());
        assertEquals(TENANT_ID, auditLogRequest.actorTenantId());
        assertEquals(AuditAction.USER_DELETE, auditLogRequest.action());
        assertEquals("USER", auditLogRequest.targetResourceType());
        assertEquals(targetUser.getuId(), auditLogRequest.targetResourceId());
        assertEquals(expectedTargetTenantId, auditLogRequest.targetTenantId());
        assertEquals(AuditDecision.DENY, auditLogRequest.decision());
        assertEquals(expectedReason, auditLogRequest.reason());
    }
}
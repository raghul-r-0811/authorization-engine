package org.raghul.auth_engine.service;

import lombok.RequiredArgsConstructor;
import org.raghul.auth_engine.dto.*;
import org.raghul.auth_engine.entity.*;
import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final RolesRepo rolesRepo;
    private final TenantRepo tenantRepo;
    private final PermissionRepo permissionRepo;
    private final TenantAuthorizationService tenantAuthorizationService;
    private final UserRoleService userRoleService;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @PreAuthorize("hasAuthority('USER_CREATE')")
    @Transactional
    public UserEntity registerUser(RegisterUserRequest newUser) {

        RolesEntity currentRole = rolesRepo.findById(newUser.roleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid role id: " + newUser.roleId()
                ));

        TenantEntity currentTenant = tenantRepo.findById(newUser.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid tenant id: " + newUser.tenantId()
                ));

        CurrentUserService.CurrentActor actor =
                currentUserService.getCurrentActor();

        Integer targetTenantId = currentTenant.getTenantId();

        try {
            tenantAuthorizationService.verifyCanAccessTenant(targetTenantId);
        } catch (AccessDeniedException exception) {

            auditLogService.logDenied(new AuditLogRequest(actor.userId(), actor.tenantId(),
                    AuditAction.USER_CREATE,"USER",null,
                    targetTenantId,AuditDecision.DENY,exception.getMessage()
            ));

            throw exception;
        }

        if (userRepo.existsByTenantAndEmail(currentTenant, newUser.u_email())) {
            throw new IllegalArgumentException(
                    "Email already exists in this tenant"
            );
        }

        UserEntity user = new UserEntity();
        user.setEmail(newUser.u_email());
        user.setPassword(passwordEncoder.encode(newUser.password()));
        user.setName(newUser.u_name());
        user.setTenant(currentTenant);

        UserEntity savedUser = userRepo.save(user);

        userRoleService.assignRoleToUser(savedUser, currentRole, currentTenant);

        auditLogService.log(new AuditLogRequest(actor.userId(),actor.tenantId(),AuditAction.USER_CREATE,
                "USER",savedUser.getuId(),targetTenantId,
                AuditDecision.ALLOW,"User created successfully"));

        return savedUser;
    }

    @PreAuthorize("hasAuthority('USER_DELETE')")
    @Transactional
    public UserEntity deleteUser(DeleteUserRequest deleteUserRequest) {

        UserEntity targetUser = userRepo.findById(deleteUserRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + deleteUserRequest.userId()
                ));

        Integer targetUserId = targetUser.getuId();

        Integer targetTenantId = (targetUser.getTenant() == null) ? null : targetUser.getTenant().getTenantId();

        CurrentUserService.CurrentActor actor = currentUserService.getCurrentActor();

        try {
            if (targetUser.getTenant() == null) {
                tenantAuthorizationService.verifyCanAccessPlatform();
            } else {
                tenantAuthorizationService.verifyCanAccessTenant(targetTenantId);
            }
        } catch (AccessDeniedException exception) {

            auditLogService.logDenied(new AuditLogRequest(actor.userId(),actor.tenantId(),
                    AuditAction.USER_DELETE,"USER",targetUserId,
                    targetTenantId, AuditDecision.DENY,exception.getMessage()));

            throw exception;
        }

        userRepo.delete(targetUser);

        auditLogService.log(new AuditLogRequest(actor.userId(),actor.tenantId(),
                AuditAction.USER_DELETE,"USER",targetUserId,
                targetTenantId, AuditDecision.ALLOW,"User deleted successfully"
        ));

        return targetUser;
    }

    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @Transactional
    public RolesEntity createNewRole(RegisterRoleRequest newRole) {

        RolesEntity currRole = new RolesEntity();
        currRole.setRoleName(newRole.roleName());
        currRole.setDescription(newRole.description());
        currRole.setScopeType(newRole.scope());

        if (newRole.scope() == ScopeType.TENANT) {

            if (newRole.tenantId() == null) {
                throw new IllegalArgumentException(
                        "Tenant ID is required for a tenant-scoped role"
                );
            }

            tenantAuthorizationService.verifyCanAccessTenant(newRole.tenantId());

            TenantEntity currentTenant = tenantRepo.findById(newRole.tenantId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Invalid tenant id: " + newRole.tenantId()
                    ));

            currRole.setTenant(currentTenant);

        } else if (newRole.scope() == ScopeType.PLATFORM) {
            tenantAuthorizationService.verifyCanAccessPlatform();
            currRole.setTenant(null);

        } else {
            throw new IllegalArgumentException("Invalid role scope");
        }

        return rolesRepo.save(currRole);
    }

    @PreAuthorize("hasAuthority('ROLE_PERMISSION_SET')")
    @Transactional
    public List<RolePermissionEntity> setNewPermissionsToRole(SetPermissionRequest request) {

        RolesEntity role = rolesRepo.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.roleId()));

        Integer targetRoleId = role.getRoleId();
        Integer targetTenantId = (role.getTenant() == null) ? null : role.getTenant().getTenantId();

        CurrentUserService.CurrentActor actor = currentUserService.getCurrentActor();

        try {
            if (role.getScopeType() == ScopeType.PLATFORM) {
                tenantAuthorizationService.verifyCanAccessPlatform();
            } else {
                tenantAuthorizationService.verifyCanAccessTenant(targetTenantId);
            }
        } catch (AccessDeniedException exception) {
            auditLogService.logDenied(new AuditLogRequest(actor.userId(), actor.tenantId(),
                    AuditAction.ROLE_PERMISSION_ASSIGN,"ROLE",
                    targetRoleId, targetTenantId, AuditDecision.DENY, exception.getMessage()));

            throw exception;
        }

        Set<String> requestedNames = request.permissionSet();

        List<PermissionEntity> foundPermissions =
                permissionRepo.findByPermissionNameIn(requestedNames);

        Set<String> foundNames = foundPermissions.stream()
                .map(PermissionEntity::getPermissionName)
                .collect(Collectors.toSet());

        Set<String> missingNames = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .collect(Collectors.toSet());

        if (!missingNames.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Permissions not found: " + missingNames
            );
        }

        for (PermissionEntity permission : foundPermissions) {
            PermissionApplicability applicability =
                    permission.getApplicability();

            if (applicability == PermissionApplicability.PLATFORM_ONLY
                    && role.getScopeType() != ScopeType.PLATFORM) {
                throw new IllegalArgumentException(
                        "Platform-only permission cannot be assigned to a tenant role: "
                                + permission.getPermissionName()
                );
            }

            if (applicability == PermissionApplicability.TENANT_ONLY
                    && role.getScopeType() != ScopeType.TENANT) {
                throw new IllegalArgumentException(
                        "Tenant-only permission cannot be assigned to a platform role: "
                                + permission.getPermissionName()
                );
            }
        }

        Set<String> alreadyAssignedNames = role.getRolePermission().stream()
                .map(rolePermission ->
                        rolePermission.getPermission().getPermissionName()
                )
                .collect(Collectors.toSet());

        List<RolePermissionEntity> newAssignments = new ArrayList<>();

        for (PermissionEntity permission : foundPermissions) {
            if (!alreadyAssignedNames.contains(permission.getPermissionName())) {
                RolePermissionEntity rolePermission =
                        new RolePermissionEntity();

                rolePermission.setRole(role);
                rolePermission.setPermission(permission);

                role.getRolePermission().add(rolePermission);
                newAssignments.add(rolePermission);
            }
        }

        rolesRepo.save(role);

        for (RolePermissionEntity assignment : newAssignments) {
            PermissionEntity permission = assignment.getPermission();

            auditLogService.log(new AuditLogRequest(actor.userId(), actor.tenantId(),
                    AuditAction.ROLE_PERMISSION_ASSIGN, "ROLE",
                    targetRoleId, targetTenantId, AuditDecision.ALLOW,
                    "Permission assigned successfully: permissionId="
                            + permission.getPermissionId() + ", permissionName="
                            + permission.getPermissionName()
            ));
        }

        return newAssignments;
    }
}
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final RolesRepo rolesRepo;
    private final TenantRepo tenantRepo;
    private final TenantAccessPolicy tenantAccessPolicy;
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

        CurrentUserService.CurrentActor actor = currentUserService.getCurrentActor();

        Integer targetTenantId = currentTenant.getTenantId();

        try {
            tenantAccessPolicy.verifyCanAccessTenant(targetTenantId);
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
                tenantAccessPolicy.verifyCanAccessPlatform();
            } else {
                tenantAccessPolicy.verifyCanAccessTenant(targetTenantId);
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


}
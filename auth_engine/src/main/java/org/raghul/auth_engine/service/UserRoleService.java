package org.raghul.auth_engine.service;

import lombok.RequiredArgsConstructor;
import org.raghul.auth_engine.dto.AssignRoleRequest;
import org.raghul.auth_engine.dto.AuditLogRequest;
import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.ScopeType;
import org.raghul.auth_engine.entity.TenantEntity;
import org.raghul.auth_engine.entity.UserEntity;
import org.raghul.auth_engine.entity.UserRoleEntity;
import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.repository.UserRoleRepo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final TenantAuthorizationService tenantAuthorizationService;
    private final UserRoleRepo userRoleRepo;
    private final UserRepo userRepo;
    private final RolesRepo rolesRepo;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    @Transactional
    public UserRoleEntity addRoleToExistingUser(AssignRoleRequest request) {

        UserEntity user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.userId()
                ));
        Integer targetUserId = user.getuId();
        Integer targetTenantId = (user.getTenant() == null) ? null : user.getTenant().getTenantId();
        CurrentUserService.CurrentActor actor = currentUserService.getCurrentActor();

        try {
            if (user.getTenant() == null) {
                tenantAuthorizationService.verifyCanAccessPlatform();
            } else {
                tenantAuthorizationService.verifyCanAccessTenant(targetTenantId);
            }
        } catch (AccessDeniedException exception) {
            auditLogService.logDenied(new AuditLogRequest(actor.userId(),
                    actor.tenantId(), AuditAction.USER_ROLE_ASSIGN, "USER",
                    targetUserId, targetTenantId, AuditDecision.DENY, exception.getMessage()
            ));
            throw exception;
        }

        RolesEntity role = rolesRepo.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Role not found with id: " + request.roleId()
                ));

        return assignRoleToUser(user, role, user.getTenant());
    }

    public UserRoleEntity assignRoleToUser(UserEntity user, RolesEntity role, TenantEntity assignmentTenant) {
        validateUserRoleAssignment(user, role, assignmentTenant);

        boolean alreadyAssigned = userRoleRepo
                .existsByUserAndRoleAndTenant(user, role, assignmentTenant);

        if (alreadyAssigned) {
            throw new IllegalArgumentException("This role is already assigned to the user");
        }

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setTenant(assignmentTenant);

        UserRoleEntity savedAssignment = userRoleRepo.save(userRole);

        CurrentUserService.CurrentActor actor = currentUserService.getCurrentActor();

        Integer targetTenantId = (user.getTenant() == null) ? null : user.getTenant().getTenantId();

        auditLogService.log(new AuditLogRequest(actor.userId(), actor.tenantId(),
                AuditAction.USER_ROLE_ASSIGN, "USER", user.getuId(),
                targetTenantId, AuditDecision.ALLOW, "Role assigned successfully: roleId=" + role.getRoleId()
                        + ", roleName=" + role.getRoleName()
        ));

        return savedAssignment;
    }

    /**
     * Validates whether the role scope and tenant match the target user.
     */
    private void validateUserRoleAssignment(UserEntity user, RolesEntity role, TenantEntity assignmentTenant) {

        if (role.getScopeType() == ScopeType.PLATFORM) {
            if (assignmentTenant != null) {
                throw new IllegalArgumentException("Platform role cannot have tenant");
            }
            if (user.getTenant() != null) {
                throw new IllegalArgumentException("Platform user cannot have tenant");
            }
            return;
        }
        if (role.getScopeType() == ScopeType.TENANT) {
            if (assignmentTenant == null) {
                throw new IllegalArgumentException("Tenant role requires tenant");
            }
            if (user.getTenant() == null) {
                throw new IllegalArgumentException("Tenant user must belong to a tenant");
            }
            if (!user.getTenant().getTenantId().equals(assignmentTenant.getTenantId())) {
                throw new IllegalArgumentException("User tenant mismatch");
            }
            if (role.getTenant() == null || !role.getTenant().getTenantId().equals(assignmentTenant.getTenantId())) {
                throw new IllegalArgumentException("Role tenant mismatch");
            }
        }
    }


}
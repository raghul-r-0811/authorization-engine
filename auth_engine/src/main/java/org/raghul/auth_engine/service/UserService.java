package org.raghul.auth_engine.service;

import org.raghul.auth_engine.dto.*;
import org.raghul.auth_engine.entity.*;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    //@Autowired
    public UserRepo userRepo;
    @Autowired
    private RolesRepo rolesRepo;
    @Autowired
    private TenantRepo tenantRepo;
    @Autowired
    private UserRoleRepo userRoleRepo;
    @Autowired
    PermissionRepo permissionRepo;
    @Autowired
    TenantAuthorizationService tenantAuthorizationService;


    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @PreAuthorize("hasAuthority('USER_CREATE')")
    @Transactional
    public boolean registerUser(RegisterUserRequest newUser) {

        RolesEntity currentRole = rolesRepo.findById(newUser.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid role id: " + newUser.roleId()));

        TenantEntity currentTenant = tenantRepo.findById(newUser.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid tenant id: " + newUser.tenantId()));

        tenantAuthorizationService.verifyCanAccessTenant(currentTenant.getTenantId());

        if (userRepo.existsByTenantAndEmail(currentTenant, newUser.u_email())) {
            throw new IllegalArgumentException("Email already exists in this tenant");
        }
        /**
         * ------------cross tenant action check-----------
         * **/


        UserEntity user = new UserEntity();
        user.setEmail(newUser.u_email());
        user.setPassword(passwordEncoder.encode(newUser.password()));
        user.setName(newUser.u_name());
        user.setTenant(currentTenant);

        UserEntity savedUser = userRepo.save(user);

        assignRoleToUser(savedUser, currentRole, currentTenant);

        return true;
    }

    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    @Transactional
    public UserRoleEntity addRoleToExistingUser(AssignRoleRequest request) {

        UserEntity user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));


        /**
         * ------------cross tenant action check-----------
         **/
        if (user.getTenant() == null) {
            tenantAuthorizationService.verifyCanAccessPlatform();
        } else {
            tenantAuthorizationService.verifyCanAccessTenant(user.getTenant().getTenantId());
        }

        RolesEntity role = rolesRepo.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.roleId()));

        TenantEntity tenant = user.getTenant();

        return assignRoleToUser(user, role, tenant);
    }


    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    private UserRoleEntity assignRoleToUser(UserEntity user, RolesEntity role, TenantEntity assignmentTenant) {

        validateUserRoleAssignment(user, role, assignmentTenant);

        boolean alreadyAssigned = userRoleRepo.existsByUserAndRoleAndTenant(user, role, assignmentTenant);
        if (alreadyAssigned) {
            throw new IllegalArgumentException("This role is already assigned to the user");
        }

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUser(user);
        userRole.setRole(role);
        userRole.setTenant(assignmentTenant);

        return userRoleRepo.save(userRole);
    }

    //fuction for userLogin for any organization
   /* public boolean login(RegisterUserRequest loginUser){
        // for given pw and email cross check whether
        return false;
    }*/

    /**
     * Whether this role with this scope is valid for user with tenant.
     * **/
    private void validateUserRoleAssignment(UserEntity user, RolesEntity role, TenantEntity assignmentTenant) {

        if (role.getScopeType() == ScopeType.PLATFORM) {
            if (assignmentTenant!=  null) {
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

    @PreAuthorize("hasAuthority('USER_DELETE')")
    @Transactional
    public boolean deleteUser(DeleteUserRequest deleteUserRequest) {

        UserEntity targetUser = userRepo.findById(deleteUserRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + deleteUserRequest.userId()
                ));
        /**
         * ------------cross tenant action check-----------
         **/

        if (targetUser.getTenant() == null) {
            tenantAuthorizationService.verifyCanAccessPlatform();
        } else {
            tenantAuthorizationService.verifyCanAccessTenant(
                    targetUser.getTenant().getTenantId()
            );
        }

        userRepo.delete(targetUser);
        return true;
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

        RolesEntity role = rolesRepo.findById(request.roleId()).orElseThrow(() ->
                new ResourceNotFoundException("Role not found with id: " + request.roleId()));


        if (role.getScopeType() == ScopeType.PLATFORM) {
            tenantAuthorizationService.verifyCanAccessPlatform();
        } else {
            tenantAuthorizationService.verifyCanAccessTenant(
                    role.getTenant().getTenantId()
            );
        }
        Set<String> requestedNames = request.permissionSet();

        List<PermissionEntity> foundPermissions = permissionRepo.findByPermissionNameIn(requestedNames);

        Set<String> foundNames = foundPermissions.stream()
                .map(PermissionEntity::getPermissionName)
                .collect(Collectors.toSet());

        Set<String> missingNames = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .collect(Collectors.toSet());

        if (!missingNames.isEmpty()) {
            throw new ResourceNotFoundException("Permissions not found: " + missingNames);
        }

        // reject permissions that cannot legally attach to this role's scope
        // BEFORE checking already-assigned, so a bad request fails atomically
        for (PermissionEntity permission : foundPermissions) {
            PermissionApplicability applicability = permission.getApplicability();

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
                .map(rolePermission -> rolePermission.getPermission().getPermissionName())
                .collect(Collectors.toSet());

        List<RolePermissionEntity> newAssignments = new ArrayList<>();

        for (PermissionEntity permission : foundPermissions) {
            if (!alreadyAssignedNames.contains(permission.getPermissionName())) {
                RolePermissionEntity rolePermission = new RolePermissionEntity();
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);

                role.getRolePermission().add(rolePermission);
                newAssignments.add(rolePermission);
            }
        }

        rolesRepo.save(role);
        return newAssignments;
    }

}

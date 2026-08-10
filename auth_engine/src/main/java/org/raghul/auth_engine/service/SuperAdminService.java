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

import java.util.*;
import java.util.stream.Collectors;


@Service
public class SuperAdminService {
    TenantRepo tenantRepo;
    RolesRepo rolesRepo;
    UserRepo userRepo;

    /**
     * These two(passwordEncoder and userRoleRepo) and {@link SuperAdminService#addSuperAdmin(RegisterUserRequest)}
     * method is for development conveninence.  It should be removed on deployment for safety reasons.
     **/
    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    UserRoleRepo userRoleRepo;
    @Autowired
    PermissionRepo permissionRepo;


    @Autowired
    public SuperAdminService(TenantRepo tenantRepo, RolesRepo rolesRepo, UserRepo userRepo) {
        this.tenantRepo = tenantRepo;
        this.rolesRepo = rolesRepo;
        this.userRepo = userRepo;
    }


    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public UserEntity addSuperAdmin(RegisterUserRequest newUser) {

        RolesEntity currentRole = rolesRepo.findById(newUser.roleId()).orElseThrow(() -> new RuntimeException("Invalid role id"));
        /**
         * When trying to create a super Admin user this check prevents it from assigning a Tenant Scoped role.
         **/
        if(currentRole.getScopeType() != ScopeType.PLATFORM){
            throw new IllegalArgumentException("You are trying to create a super admin, it needs a PLATFORM scoped Role");
        }

        if(userRepo.existsByEmail(newUser.u_email())){
            throw  new IllegalArgumentException("Email already exists");
        }
        UserEntity user = new UserEntity();
        user.setEmail(newUser.u_email());
        user.setPassword(passwordEncoder.encode(newUser.password()));
        user.setName(newUser.u_name());
        user.setTenant(null);

        UserEntity currentUser = userRepo.save(user);

        UserRoleEntity currentUserRole = new UserRoleEntity();
        currentUserRole.setRole(currentRole);
        currentUserRole.setUser(currentUser);
        userRoleRepo.save(currentUserRole);
        return currentUser;
    }


    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public TenantEntity addNewTenant(RegisterTenantRequest newTenant) {
        TenantEntity currTenant = new TenantEntity();
        currTenant.setTenantName(newTenant.tenantName());
        return tenantRepo.save(currTenant);
    }
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public RolesEntity createNewRole(RegisterRoleRequest newRole) {

        RolesEntity currRole = new RolesEntity();
        currRole.setRoleName(newRole.roleName());::wq
        currRole.setDescription(newRole.description());
        currRole.setScopeType(newRole.scope());

        if (newRole.scope() == ScopeType.TENANT) {
            TenantEntity currentTenant = tenantRepo.findById(newRole.tenantId())
                    .orElseThrow(() -> new RuntimeException("Invalid tenant id"));
            currRole.setTenant(currentTenant);
        } else if (newRole.scope() == ScopeType.PLATFORM) {
            currRole.setTenant(null);
        } else {
            throw new RuntimeException("Invalid scope");
        }
        return rolesRepo.save(currRole);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PermissionEntity createNewPermission(RegisterPermissionRequest newPermission) {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setDescription(newPermission.description());
        permissionEntity.setPermissionName(newPermission.permissionName());
        return permissionRepo.save(permissionEntity);
    }



    @PreAuthorize("hasAuthority('SET_PERMISSION')")
    @Transactional
    public List<RolePermissionEntity> setNewPermissionsToRole(SetPermissionRequest request) {

        RolesEntity role = rolesRepo.findById(request.roleId()).orElseThrow(() ->
                        new ResourceNotFoundException("Role not found with id: " + request.roleId()));

        Set<String> requestedNames = request.permissionSet();
        System.out.println("------------------------------REACHED here-------------------------------");

        List<PermissionEntity> foundPermissions = permissionRepo.findByPermissionNameIn(requestedNames);
        System.out.println("------------------------------CROSSED here-------------------------------");
        Set<String> foundNames = foundPermissions.stream()
                .map(PermissionEntity::getPermissionName)
                .collect(Collectors.toSet());

        Set<String> missingNames = requestedNames.stream()
                .filter(name -> !foundNames.contains(name))
                .collect(Collectors.toSet());

        if (!missingNames.isEmpty()) {
            for(String str : missingNames){
                System.out.println("----------------- missing permission name ---------- :"+str);
            }
            throw new ResourceNotFoundException("Permissions not found: " + missingNames);
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

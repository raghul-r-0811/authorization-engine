package org.raghul.auth_engine.service;

import org.raghul.auth_engine.dto.RegisterRoleRequest;
import org.raghul.auth_engine.dto.RegisterTenantRequest;
import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.entity.*;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.TenantRepo;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.repository.UserRoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    public SuperAdminService(TenantRepo tenantRepo, RolesRepo rolesRepo, UserRepo userRepo) {
        this.tenantRepo = tenantRepo;
        this.rolesRepo = rolesRepo;
        this.userRepo = userRepo;
    }

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

    public RolesEntity addNewRole(RegisterRoleRequest newRole) {

        RolesEntity currRole = new RolesEntity();
        currRole.setRoleName(newRole.roleName());
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

}

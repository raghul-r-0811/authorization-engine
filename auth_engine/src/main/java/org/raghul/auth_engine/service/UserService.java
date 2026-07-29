package org.raghul.auth_engine.service;

import org.raghul.auth_engine.dto.AssignRoleRequest;
import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.entity.*;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.TenantRepo;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.repository.UserRoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Transactional
    public boolean registerUser(RegisterUserRequest newUser) {

        RolesEntity currentRole = rolesRepo.findById(newUser.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid role id: " + newUser.roleId()));

        TenantEntity currentTenant = tenantRepo.findById(newUser.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid tenant id: " + newUser.tenantId()));

        if (userRepo.existsByTenantAndEmail(currentTenant, newUser.u_email())) {
            throw new IllegalArgumentException("Email already exists in this tenant");
        }

        UserEntity user = new UserEntity();
        user.setEmail(newUser.u_email());
        user.setPassword(passwordEncoder.encode(newUser.password()));
        user.setName(newUser.u_name());
        user.setTenant(currentTenant);

        UserEntity savedUser = userRepo.save(user);

        assignRoleToUser(savedUser, currentRole, currentTenant);

        return true;
    }

    @Transactional
    public UserRoleEntity addRoleToExistingUser(AssignRoleRequest request) {

        UserEntity user = userRepo.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        RolesEntity role = rolesRepo.findById(request.roleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + request.roleId()));

        TenantEntity tenant = null;
        if (request.tenantId() != null) {
            tenant = tenantRepo.findById(request.tenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + request.tenantId()));
        }

        return assignRoleToUser(user, role, tenant);
    }

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
    public boolean login(RegisterUserRequest loginUser){
        // for given pw and email cross check whether
        return false;
    }

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

}

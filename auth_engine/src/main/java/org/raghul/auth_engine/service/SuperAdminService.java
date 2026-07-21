package org.raghul.auth_engine.service;

import org.raghul.auth_engine.dto.RegisterRoleRequest;
import org.raghul.auth_engine.dto.RegisterTenantRequest;
import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.TenantEntity;
import org.raghul.auth_engine.repository.RolesRepo;
import org.raghul.auth_engine.repository.TenantRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SuperAdminService {
    TenantRepo tenantRepo;
    RolesRepo rolesRepo;

    @Autowired
    public SuperAdminService(TenantRepo tenantRepo,RolesRepo rolesRepo){
        this.tenantRepo = tenantRepo;
        this.rolesRepo = rolesRepo;
    }

    public TenantEntity addNewTenant(RegisterTenantRequest newTenant){
        TenantEntity currTenant = new TenantEntity();
        currTenant.setTenantName(newTenant.tenantName());
        return tenantRepo.save(currTenant);
    }

    public RolesEntity addNewRole(RegisterRoleRequest newRole){
        RolesEntity currRole = new RolesEntity();
        currRole.setRoleName(newRole.roleName());
        currRole.setDescription(newRole.description());
        currRole.setScopeType(newRole.scope());
        currRole.setTenantId(newRole.tenantId());
        return rolesRepo.save(currRole);
        //return "new role added to roles table by SuperAdmin";
    }
}

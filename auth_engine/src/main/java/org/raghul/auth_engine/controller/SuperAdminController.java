package org.raghul.auth_engine.controller;


import org.raghul.auth_engine.dto.ApiResponse;
import org.raghul.auth_engine.dto.RegisterRoleRequest;
import org.raghul.auth_engine.dto.RegisterTenantRequest;
import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.TenantEntity;
import org.raghul.auth_engine.service.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/admin")
public class SuperAdminController {
    @Autowired
    SuperAdminService superAdminService;

    //add a new Tenant in the tenant table.
    @PostMapping("/addTenant")
    public ResponseEntity<ApiResponse> addTenant(@RequestBody RegisterTenantRequest newTenant){
        TenantEntity savedTenant =  superAdminService.addNewTenant(newTenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("New Tenant created",savedTenant));
    }

    //add a new role(tenanta_admin) in the roles table
    /*
 {
  "roleName": "SUPER_ADMIN",
  "description": "Platform-level super administrator with full control over tenants, users, roles, and global configuration. Can manage all organizations but does not belong to any tenant.",
  "scope": "PLATFORM"
}
    */


    @PostMapping("/addRoles")
    public ResponseEntity<ApiResponse> addTenantAdminRole(@RequestBody RegisterRoleRequest newRole){
        RolesEntity savedRole = superAdminService.addNewRole(newRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Role created successfully", savedRole));
    }

}

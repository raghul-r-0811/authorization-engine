package org.raghul.auth_engine.controller;


import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.raghul.auth_engine.dto.*;
import org.raghul.auth_engine.entity.*;
import org.raghul.auth_engine.service.SuperAdminService;
import org.raghul.auth_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/admin")
public class SuperAdminController {
    @Autowired
    SuperAdminService superAdminService;

    @Autowired
    UserService userService;

    //add a new Tenant in the tenant table.
   // @PreAuthorize("hasRole('SUPER_ADMIN')")

    @PostMapping("/registerSuperAdminUser")
    public String addSuperAdminUser(@RequestBody RegisterUserRequest newUser){
        superAdminService.addSuperAdmin(newUser);
        return "New Admin added";
    }

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






    @PostMapping("/createPermission")
    public ResponseEntity<ApiResponse> createPermission(@RequestBody RegisterPermissionRequest newPermission){
        PermissionEntity createdPermission =  superAdminService.createNewPermission(newPermission);
        return  ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("New Permission created",createdPermission));
    }
    /**
     *Used to set a permissions to a role.
     **/




}

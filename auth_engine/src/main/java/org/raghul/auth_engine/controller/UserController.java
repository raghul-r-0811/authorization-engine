package org.raghul.auth_engine.controller;


import lombok.RequiredArgsConstructor;
import org.raghul.auth_engine.dto.*;
import org.raghul.auth_engine.entity.RolePermissionEntity;
import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.UserEntity;
import org.raghul.auth_engine.entity.UserRoleEntity;
import org.raghul.auth_engine.service.RoleService;
import org.raghul.auth_engine.service.UserRoleService;
import org.raghul.auth_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("app/user/")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;


    @PostMapping("/deleteUser")
    public ResponseEntity<ApiResponse> deleteUser(@RequestBody DeleteUserRequest deleteUserRequest){
        UserEntity deletedUser = userService.deleteUser(deleteUserRequest);
        UserResponse userResponse = new UserResponse(
                deletedUser.getuId(),
                deletedUser.getName(),
                deletedUser.getEmail(),
                deletedUser.getTenant().getTenantId()
        );
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ApiResponse("Role assigned to user successfully", userResponse));
    }

    @PostMapping("/createRole")
    public ResponseEntity<ApiResponse> addTenantAdminRole(@RequestBody RegisterRoleRequest newRole){
        RolesEntity savedRole = roleService.createNewRole(newRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Role created successfully", savedRole));
    }

    @PostMapping("setPermission")
    public ResponseEntity<ApiResponse> setPermissionToRole(@RequestBody SetPermissionRequest setPermissionRequest){
        List<RolePermissionEntity> saved = roleService.setNewPermissionsToRole(setPermissionRequest);

        List<RolePermissionResponse> response = saved.stream()
                .map(rp -> new RolePermissionResponse(
                        rp.getRole().getRoleId(),
                        rp.getRole().getRoleName(),
                        rp.getPermission().getPermissionId(),
                        rp.getPermission().getPermissionName()
                ))
                .toList();


        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("All permissions are set to the givenRole",response));
    }


    @PostMapping("/assignRole")
    public ResponseEntity<ApiResponse> assignRoleToExistingUser(@RequestBody AssignRoleRequest request) {
        UserRoleEntity assignedRole = userRoleService.addRoleToExistingUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Role assigned to user successfully", assignedRole));
    }

}

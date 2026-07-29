package org.raghul.auth_engine.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name ="role_permission",uniqueConstraints={@UniqueConstraint(columnNames = { "role_id","permission_id"})})
public class RolePermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer rolePermissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="role_id",nullable = false)
    RolesEntity role ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="permission_id", nullable = false)
    PermissionEntity permission;


    public Integer getRolePermissionId() {
        return rolePermissionId;
    }

    public void setRolePermissionId(Integer rolePermissionId) {
        this.rolePermissionId = rolePermissionId;
    }

    public RolesEntity getRole() {
        return role;
    }

    public void setRole(RolesEntity role) {
        this.role = role;
    }

    public PermissionEntity getPermission() {
        return permission;
    }

    public void setPermission(PermissionEntity permission) {
        this.permission = permission;
    }
}

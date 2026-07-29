package org.raghul.auth_engine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

@Entity
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer permissionId;

    @NotNull
    @Column(nullable = false,unique = true)
    private String permissionName;

    private String description;

    @OneToMany(mappedBy = "permission")
    private Set<RolePermissionEntity> rolePermission = new HashSet<>();

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public Integer getPermissionId() {
        return permissionId;
    }

    private void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<RolePermissionEntity> getRolesPermission() {
        return rolePermission;
    }

    public void setRolesPermission(Set<RolePermissionEntity> rolePermission) {
        this.rolePermission = rolePermission;
    }
}

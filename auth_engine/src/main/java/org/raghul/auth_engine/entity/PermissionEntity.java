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
    @Column(nullable = false, unique = true)
    private String permissionName;

    private String description;

    //determines whether this permission may be attached to a PLATFORM role, a TENANT role, or both
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionApplicability applicability;

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


    public PermissionApplicability getApplicability() {
        return applicability;
    }


    public void setApplicability(PermissionApplicability applicability) {
        this.applicability = applicability;
    }

    public Set<RolePermissionEntity> getRolesPermission() {
        return rolePermission;
    }

    public void setRolesPermission(Set<RolePermissionEntity> rolePermission) {
        this.rolePermission = rolePermission;
    }
}
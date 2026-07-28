package org.raghul.auth_engine.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name ="roles_entity",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "roleName"})
})
public class RolesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int roleId;
    @Column(nullable = false)
    String roleName;

    /**
     * Many roles will be in a single tenant.
     * And 2 different tenant will not have same role. Roles are tenant specific.
     * **/
    @ManyToOne(fetch = FetchType.LAZY)
            @JoinColumn(name="tenant_id")
    TenantEntity tenant;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScopeType scopeType;

    String description;

    /**
     *
     * **/
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();

    @ManyToMany
            @JoinTable(name = "role_permission",joinColumns = @JoinColumn(name ="role_id"),
            inverseJoinColumns = @JoinColumn(name ="permission_id"))
    private Set<PermissionEntity> permissions = new HashSet<>();



    public int getRoleId() {
        return roleId;
    }

    public @NotNull String getRoleName() {
        return roleName;
    }

    public void setRoleName(@NotNull String roleName) {
        this.roleName = roleName;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public Set<UserRoleEntity> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<UserRoleEntity> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<PermissionEntity> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionEntity> permissions) {
        permissions = permissions;
    }
/*public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }*/

    public @NotNull ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(@NotNull ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

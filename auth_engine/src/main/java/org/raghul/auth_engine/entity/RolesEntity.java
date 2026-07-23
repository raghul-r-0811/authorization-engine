package org.raghul.auth_engine.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "roleName"})
})
public class RolesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int roleId;
    String roleName;
    Integer tenantId;
    @NotNull
    private String scopeType;
    String description;

    public int getRoleId() {
        return roleId;
    }

    public @NotNull String getRoleName() {
        return roleName;
    }

    public void setRoleName(@NotNull String roleName) {
        this.roleName = roleName;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public @NotNull String getScopeType() {
        return scopeType;
    }

    public void setScopeType(@NotNull String scopeType) {
        this.scopeType = scopeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

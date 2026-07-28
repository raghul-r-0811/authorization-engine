package org.raghul.auth_engine.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;


/**
 * It is join table representing what are the roles each user holds.
 *
 * One user can have multiple roles :
 * which is know using {@link org.raghul.auth_engine.entity.UserRoleEntity#role}
 *
 * **/

@Entity

/**
 * Logically Users and Roles are teanant specific, same user wont be in 2 tenants and
 * the same goes for roles too. But here we are still giving tenent_id as unique constraints just
 * to be safety for future use.
 * **/
@Table(
        name = "user_role",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "role_id", "tenant_id"})
        }
)
public class UserRoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userRoleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RolesEntity role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntity tenant;

    private LocalDateTime grantedAt;

    private Integer grantedBy;

    public Integer getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(Integer userRoleId) {
        this.userRoleId = userRoleId;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public RolesEntity getRole() {
        return role;
    }

    public void setRole(RolesEntity role) {
        this.role = role;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public Integer getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(Integer grantedBy) {
        this.grantedBy = grantedBy;
    }
}

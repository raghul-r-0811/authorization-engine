package org.raghul.auth_engine.entity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/** Main user entity  **/

@Entity
@Table(
        name = "users_entity",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenantId", "email"})
})

public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer uId;

    String name;
    String password;

    @Column(nullable = false)
    String email;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    TenantEntity tenant;

    /** One user can have multiple roles.And each of those roles
     * will have its own set of permissions for differnt resources **/
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleEntity> userRoles = new HashSet<>();



    public Integer getuId() {
        return uId;
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

    public void setuId(Integer uId) {
        this.uId = uId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*public RolesEntity getRole() {
        return role;
    }

    public void setRole(RolesEntity role) {
        this.role = role;
    }*/

    /*public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }*/

    /*public void setRole(String role) {  this.role = role; }*/
}


/*

A user belongs to a Organization and he will have a particular role and multiple attributes in that organization
Multiple user will be there from multiple Organization.
Each organization will have organization specific roles and attributes


UserID + OrgID + Role(org specific) + Attribute(org specific) based on these values these user will be authorized to
access specific url endpoints

Main User table will have 
*/




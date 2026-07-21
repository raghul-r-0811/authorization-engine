package org.raghul.auth_engine.entity;
import jakarta.persistence.*;


@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"org_id", "email"})
})

public class UserEnity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer u_id;
    String name;
    String password;
    @Column(nullable = false)
    String email;
    Integer role;

    @Column(nullable = false)
    Integer org_id;

    public Integer getOrg_id() {
        return org_id;
    }

    public void setOrg_id(int org_id) {
        this.org_id = org_id;
    }

    public Integer getU_id() {
        return u_id;
    }

    /*public void setU_id(int u_id) {
        this.u_id = u_id;
    }*/

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




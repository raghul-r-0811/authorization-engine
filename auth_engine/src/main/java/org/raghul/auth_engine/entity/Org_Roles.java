package org.raghul.auth_engine.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Org_Roles {
    @Id
    int user_id;
    @Id
    int org_id;
    String role;

}

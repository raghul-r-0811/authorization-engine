package org.raghul.auth_engine.entity;
import jakarta.persistence.*;

@Entity

public class Organization {
    @Id
    int org_id;
    String name;

    //make a enum for this later
    int status;
    String slug;

}

package org.raghul.auth_engine.entity;


import jakarta.persistence.*;

//only super admin can add tenants to this table

@Entity
public class TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tenantId;

    @Column(nullable = false, unique = true)
    private String tenantName;

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }
}

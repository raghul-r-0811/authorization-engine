package org.raghul.auth_engine.entity.audit;


import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "authorization_audit_log")
public class AuthorizationAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt; //Exact time this event was recorded

    @Column(nullable = false)
    private Integer actorUserId; //The authenticated user who attempted/performed the operation

    private Integer actorTenantId; //Tenant the actor belongs to; null for platform-level SuperAdmin

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action; //A controlled enum naming the business action

    @Column(nullable = false)
    private String targetResourceType; //What kind of resource was affected(User or Role or Permission some DB entry)


    private Integer targetResourceId; //The specific affected resource(userId or roleId or etc)

    private Integer targetTenantId; //Tenant owning the affected resource; null for platform resources/users

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditDecision decision; //Whether authorization/action was allowed or denied

    @Column(nullable = false)
    private String reason;


    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Integer getActorUserId() {
        return actorUserId;
    }

    @PrePersist
    private void setCreatedAt() {
        this.createdAt = Instant.now();
    }

    public void setActorUserId(Integer actorUserId) {
        this.actorUserId = actorUserId;
    }

    public Integer getActorTenantId() {
        return actorTenantId;
    }

    public void setActorTenantId(Integer actorTenantId) {
        this.actorTenantId = actorTenantId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getTargetResourceType() {
        return targetResourceType;
    }

    public void setTargetResourceType(String targetResourceType) {
        this.targetResourceType = targetResourceType;
    }

    public Integer getTargetResourceId() {
        return targetResourceId;
    }

    public void setTargetResourceId(Integer targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    public Integer getTargetTenantId() {
        return targetTenantId;
    }

    public void setTargetTenantId(Integer targetTenantId) {
        this.targetTenantId = targetTenantId;
    }

    public AuditDecision getDecision() {
        return decision;
    }

    public void setDecision(AuditDecision decision) {
        this.decision = decision;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

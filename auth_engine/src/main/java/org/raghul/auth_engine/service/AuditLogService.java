package org.raghul.auth_engine.service;

import lombok.RequiredArgsConstructor;
import org.raghul.auth_engine.dto.AuditLogRequest;
import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;
import org.raghul.auth_engine.entity.audit.AuthorizationAuditLogEntity;
import org.raghul.auth_engine.repository.AuditLogRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepo auditLogRepo;

    public void log(AuditLogRequest auditLogRequest)
    {
        AuthorizationAuditLogEntity auditLog = new AuthorizationAuditLogEntity();

        auditLog.setActorUserId(auditLogRequest.actorUserId());
        auditLog.setActorTenantId(auditLogRequest.actorTenantId());
        auditLog.setAction(auditLogRequest.action());
        auditLog.setTargetResourceType(auditLogRequest.targetResourceType());
        auditLog.setTargetResourceId(auditLogRequest.targetResourceId());
        auditLog.setTargetTenantId(auditLogRequest.targetTenantId());
        auditLog.setDecision(auditLogRequest.decision());
        auditLog.setReason(auditLogRequest.reason());
        System.out.println("--------------- is it saving -----------");
        auditLogRepo.save(auditLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDenied(AuditLogRequest auditLogRequest) {
        log(auditLogRequest);
    }
}

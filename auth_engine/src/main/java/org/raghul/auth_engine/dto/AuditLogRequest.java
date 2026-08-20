package org.raghul.auth_engine.dto;

import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;

public record AuditLogRequest(Integer actorUserId,
                              Integer actorTenantId,
                              AuditAction action,
                              String targetResourceType,
                              Integer targetResourceId,
                              Integer targetTenantId,
                              AuditDecision decision,
                              String reason) {
}

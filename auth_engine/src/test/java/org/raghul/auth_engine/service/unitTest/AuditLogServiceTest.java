package org.raghul.auth_engine.service.unitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raghul.auth_engine.dto.AuditLogRequest;
import org.raghul.auth_engine.entity.audit.AuditAction;
import org.raghul.auth_engine.entity.audit.AuditDecision;
import org.raghul.auth_engine.entity.audit.AuthorizationAuditLogEntity;
import org.raghul.auth_engine.repository.AuditLogRepo;
import org.raghul.auth_engine.service.AuditLogService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    private static final Integer ACTOR_USER_ID = 1;
    private static final Integer ACTOR_TENANT_ID = 10;
    private static final Integer TARGET_RESOURCE_ID = 100;
    private static final Integer TARGET_TENANT_ID = 20;

    @Mock
    private AuditLogRepo auditLogRepo;

    @InjectMocks
    private AuditLogService auditLogService;

    @Captor
    private ArgumentCaptor<AuthorizationAuditLogEntity> auditLogCaptor;

    @Test
    void log_whenAuditRequestIsProvided_mapsAllFieldsAndSavesAuditLog() {
        AuditLogRequest request = buildAuditRequest(AuditAction.USER_CREATE, AuditDecision.ALLOW, "User created successfully");

        auditLogService.log(request);

        verify(auditLogRepo).save(auditLogCaptor.capture());

        AuthorizationAuditLogEntity savedAuditLog = auditLogCaptor.getValue();

        assertEquals(ACTOR_USER_ID, savedAuditLog.getActorUserId());
        assertEquals(ACTOR_TENANT_ID, savedAuditLog.getActorTenantId());
        assertEquals(AuditAction.USER_CREATE, savedAuditLog.getAction());
        assertEquals("USER", savedAuditLog.getTargetResourceType());
        assertEquals(TARGET_RESOURCE_ID, savedAuditLog.getTargetResourceId());
        assertEquals(TARGET_TENANT_ID, savedAuditLog.getTargetTenantId());
        assertEquals(AuditDecision.ALLOW, savedAuditLog.getDecision());
        assertEquals("User created successfully", savedAuditLog.getReason());
    }

    @Test
    void logDenied_whenAuditRequestIsProvided_mapsAllFieldsAndSavesAuditLog() {
        AuditLogRequest request = buildAuditRequest(AuditAction.USER_DELETE, AuditDecision.DENY, "Cross-tenant operation is not allowed");

        auditLogService.logDenied(request);

        verify(auditLogRepo).save(auditLogCaptor.capture());

        AuthorizationAuditLogEntity savedAuditLog = auditLogCaptor.getValue();

        assertEquals(ACTOR_USER_ID, savedAuditLog.getActorUserId());
        assertEquals(ACTOR_TENANT_ID, savedAuditLog.getActorTenantId());
        assertEquals(AuditAction.USER_DELETE, savedAuditLog.getAction());
        assertEquals("USER", savedAuditLog.getTargetResourceType());
        assertEquals(TARGET_RESOURCE_ID, savedAuditLog.getTargetResourceId());
        assertEquals(TARGET_TENANT_ID, savedAuditLog.getTargetTenantId());
        assertEquals(AuditDecision.DENY, savedAuditLog.getDecision());
        assertEquals("Cross-tenant operation is not allowed", savedAuditLog.getReason());
    }

    private AuditLogRequest buildAuditRequest(AuditAction action, AuditDecision decision, String reason) {
        return new AuditLogRequest(ACTOR_USER_ID, ACTOR_TENANT_ID, action, "USER", TARGET_RESOURCE_ID, TARGET_TENANT_ID, decision, reason);
    }
}
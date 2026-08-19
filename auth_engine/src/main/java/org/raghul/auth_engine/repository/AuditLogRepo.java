package org.raghul.auth_engine.repository;

import org.raghul.auth_engine.entity.audit.AuthorizationAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepo extends JpaRepository<AuthorizationAuditLogEntity,Long> {
}

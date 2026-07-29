package org.raghul.auth_engine.repository;

import org.raghul.auth_engine.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TenantRepo extends JpaRepository<TenantEntity,Integer> {
}

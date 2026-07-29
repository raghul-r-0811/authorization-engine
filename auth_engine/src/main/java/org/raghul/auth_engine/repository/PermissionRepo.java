package org.raghul.auth_engine.repository;

import org.raghul.auth_engine.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PermissionRepo extends JpaRepository<PermissionEntity,Integer> {

    List<PermissionEntity> findByPermissionNameIn(Set<String> permissionName);
}

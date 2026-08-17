package org.raghul.auth_engine.repository;

import org.raghul.auth_engine.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepo extends JpaRepository<PermissionEntity,Integer> {

    List<PermissionEntity> findByPermissionNameIn(Set<String> permissionName);
    Optional<PermissionEntity> findByPermissionName(String permissionName);
}

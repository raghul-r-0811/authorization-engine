package org.raghul.auth_engine.repository;

import org.raghul.auth_engine.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepo extends JpaRepository<UserRoleEntity,Integer> {
}

package org.raghul.auth_engine.repository;

import org.raghul.auth_engine.entity.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepo extends JpaRepository<RolesEntity,Integer> {

}

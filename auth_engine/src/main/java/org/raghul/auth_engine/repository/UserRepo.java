package org.raghul.auth_engine.repository;


import org.raghul.auth_engine.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<UserEntity,Integer> {

    UserEntity findByEmail(String username);
}

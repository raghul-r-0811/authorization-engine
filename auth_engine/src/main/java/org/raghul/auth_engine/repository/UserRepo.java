package org.raghul.auth_engine.repository;


import org.raghul.auth_engine.entity.UserEnity2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<UserEnity2,Integer> {

    UserEnity2 findByEmail(String username);
}

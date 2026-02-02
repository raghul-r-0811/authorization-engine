package org.raghul.auth_engine.service;

import org.raghul.auth_engine.entity.UserEnity;
import org.raghul.auth_engine.repository.UserRepo;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public static UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public static boolean registerUser(UserEnity newUser){
        userRepo.save(newUser);
        return true;
    }
}

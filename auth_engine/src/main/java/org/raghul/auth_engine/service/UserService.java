package org.raghul.auth_engine.service;

import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.entity.UserEnity;
import org.raghul.auth_engine.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    //@Autowired
    public UserRepo userRepo;
    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean registerUser(RegisterUserRequest newUser){
        UserEnity user = new UserEnity();
        user.setEmail(newUser.u_email());
        user.setPassword(passwordEncoder.encode(newUser.password()));
        user.setName(newUser.u_name());
       // user.setRole(newUser.role());
        userRepo.save(user); // you were missing this!
        return true;
    }
    //fuction for userLogin for any organization
    public boolean login(RegisterUserRequest loginUser){
        // for given pw and email cross check whether
        return false;
    }
}

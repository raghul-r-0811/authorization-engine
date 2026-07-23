package org.raghul.auth_engine.service;

import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.UserEntity;
import org.raghul.auth_engine.repository.RolesRepo;
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
    private RolesRepo rolesRepo;
    @Autowired
    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean registerUser(RegisterUserRequest newUser){
        UserEntity user = new UserEntity();
        RolesEntity currentRole = rolesRepo.findById(newUser.roleId()) .orElseThrow(() -> new RuntimeException("Invalid role id"));;
        user.setEmail(newUser.u_email());
        user.setPassword(passwordEncoder.encode(newUser.password()));
        user.setName(newUser.u_name());
        user.setRole(currentRole);
        user.setTenantId(newUser.tenantId());
        userRepo.save(user); // you were missing this!
        return true;
    }
    //fuction for userLogin for any organization
    public boolean login(RegisterUserRequest loginUser){
        // for given pw and email cross check whether
        return false;
    }
}

package org.raghul.auth_engine.controller;


import org.raghul.auth_engine.dto.DeleteUserRequest;
import org.raghul.auth_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public boolean deleteUser(DeleteUserRequest deleteUserRequest){
        userService.deleteUser(deleteUserRequest);
        return  true;
    }

}

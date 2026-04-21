package org.raghul.auth_engine.controller;

import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*

=============================================================================
        HOME CONTROLLER : WHERE USER REGISTRATION OR LOGIN HAPPENS
=============================================================================

 */


@RestController
@RequestMapping("app/home")
public class Home_Controller {
    @Autowired
    UserService userService;
    @PostMapping("/register")
    public String register(@RequestBody RegisterUserRequest newUser){
        userService.registerUser(newUser);
        return "registeration done";
    }

    public String login(@RequestBody RegisterUserRequest userLogin){
        userService.login(userLogin);
        return "valid user - Login successful";
    }
}

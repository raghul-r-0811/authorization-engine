package org.raghul.auth_engine.controller;

import org.raghul.auth_engine.dto.LoginRequest;
import org.raghul.auth_engine.dto.LoginResponse;
import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.security.JwtService;
import org.raghul.auth_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;

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
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @PostMapping("/register")
    public String register(@RequestBody RegisterUserRequest newUser){
        userService.registerUser(newUser);
        return "registeration done";

        /*  Sample Entry for Postman
        {
          "u_name": "Raghul Ramar 201",
          "u_email": "Raghul201@gmail.com",
          "password": "secret123456"
        }

         */
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest userLogin){
        // if lots of unwanted(hacker attacks) login request hits the server it will degrade/break the server performance. Find ways to prevent it
        //userService.login(userLogin);   ---> this is not needed because this part is handled by spring security
        //System.out.println("------------------------------------Authenticating------------------------------------");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.email(),userLogin.password()));
        System.out.println("auth done");
        String jwtToken = jwtService.generateToken(userLogin);

        //System.out.println("--------------------------------- Authentication done -----------------------------------------");
        //return a JWT token so user can use that
        return new LoginResponse(jwtToken);
    }
}

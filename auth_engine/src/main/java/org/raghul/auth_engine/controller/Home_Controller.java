package org.raghul.auth_engine.controller;

import jakarta.validation.Valid;
import org.raghul.auth_engine.dto.LoginRequest;
import org.raghul.auth_engine.dto.LoginResponse;
import org.raghul.auth_engine.dto.RegisterUserRequest;
import org.raghul.auth_engine.security.CustomUserDetails;
import org.raghul.auth_engine.security.JwtService;
import org.raghul.auth_engine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Map;

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

    public Home_Controller(){
        System.out.println("Home_Contoller is created");
    }

    @GetMapping("/test")  // ADD THIS temporarily
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller WORKS!");
    }

    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterUserRequest newUser){
        userService.registerUser(newUser);
        return "registeration done";
    }


/*---------------------------------------------------------------------
        *//*  Sample Entry for Postman
        {
          "u_name": "Raghul Ramar 201",
          "u_email": "Raghul201@gmail.com",
          "password": "secret123456"
        }

         *//*
//---------------------------------------------------------------------

    }*/



    /**  ----------------------  Place where DaoAuthentication Manager Loaded with UserDetailServive(CustomUSerDetailService)
     * which is loaded with Usertails(CustomeUserDetails) loads user from the db .{@link DaoAuthenticationProvider}DaoAuthenticationProvider.retrieveUser **/

    /** if lots of unwanted(hacker attacks) login request hits the server it will degrade/break the server performance. Find ways to prevent it**/

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest userLogin){
        System.out.println("------------------------------------Authenticating------------------------------------");
        Authentication authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.email(),userLogin.password())); /**{@link AbstractUserDetailsAuthenticationProvider#authenticate(Authentication)}**/
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String jwtToken = jwtService.generateToken(authentication,customUserDetails);
        return new LoginResponse(jwtToken);
    }


    @GetMapping("/verifyToken")
    public Map<String,Object> checkToken(@RequestHeader("Authorization")String authHeader){
        String token = authHeader.substring(7);
        if(jwtService.isTokenValid(token)){
            return jwtService.extractUserDetailsFromToken(token);
        }
        return null;
    }
}

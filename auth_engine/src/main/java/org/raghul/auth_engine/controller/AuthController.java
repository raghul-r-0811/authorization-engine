package org.raghul.auth_engine.controller;


import org.raghul.auth_engine.entity.UserEnity2;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*

=========================================================================================
                    JUST A SAMPLE CONTROLLER TO CHECK WHETHER THINGS WORK
==========================================================================================

 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")  // ADD THIS temporarily
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Controller WORKS!");
    }


//    @PostMapping("/register")
//    public UserEnity2 register(@RequestBody UserEnity2 newUser) {
//        //change userEntity to DTO and chnage them into entity in service
//        System.out.println("registeing new User");
//
//        //userService.registerUser(newUser);
//        return newUser;
//    }




}


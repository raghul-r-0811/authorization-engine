package org.raghul.auth_engine.dto;


// DTO used for user Registration


import jakarta.validation.constraints.NotBlank;

//public record RegisterUserRequest(String u_name, String u_email, String password) { }
public record RegisterUserRequest(@NotBlank String u_name,@NotBlank String u_email,@NotBlank String password,Integer orgId) {

}
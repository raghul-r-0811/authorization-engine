package org.raghul.auth_engine.dto;


// DTO used for user Registration



public record RegisterUserRequest(String u_name, String u_email, String password) {
}

package com.fmi.quarkus.dto;

public class UserDto {

    public Long id;
    public String username;
    public String email;
    public String role;   // single role for display (e.g. "ADMIN" or "USER")

    public UserDto() {
    }

    public UserDto(Long id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}


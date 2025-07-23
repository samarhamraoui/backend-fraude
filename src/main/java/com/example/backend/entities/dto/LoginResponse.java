package com.example.backend.entities.dto;

import java.util.List;

public class LoginResponse {
    private String username;
    private UserDto user;
    private String token;
    private List<String> roles;

    public LoginResponse() {
    }

    public LoginResponse(String username, UserDto user, String token, List<String> roles) {
        this.username = username;
        this.user = user;
        this.token = token;
        this.roles = roles;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}

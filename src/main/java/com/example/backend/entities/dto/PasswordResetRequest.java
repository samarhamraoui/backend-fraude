package com.example.backend.entities.dto;

public class PasswordResetRequest {
    private String email;
    private String username;

    public PasswordResetRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}




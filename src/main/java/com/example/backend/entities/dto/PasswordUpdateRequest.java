package com.example.backend.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateRequest {
    private String token;
    private String currentPassword;
    private String newPassword;
}

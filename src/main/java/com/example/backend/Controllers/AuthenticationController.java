package com.example.backend.Controllers;

import com.example.backend.conf.JwtTokenUtil;
import com.example.backend.entities.dto.LoginRequest;
import com.example.backend.entities.dto.LoginResponse;
import com.example.backend.entities.dto.PasswordUpdateRequest;
import com.example.backend.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@Tag(name = "Authentication API", description = "Endpoints for user authentication and management")
public class AuthenticationController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "User login", description = "Authenticate user and generate JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.doLogin(loginRequest);
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + loginResponse.getToken())
                    .body(loginResponse);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String usernameOrEmail) {
        try {
            authService.forgotPassword(usernameOrEmail);
            return ResponseEntity.ok("Email was sent successfully.");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not Found on Database please check email again!");
        }

    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordUpdateRequest passwordUpdateRequest) {
        try {
            authService.resetPassword(passwordUpdateRequest.getToken(), passwordUpdateRequest.getNewPassword());
            return ResponseEntity.ok("Password has been reset successfully");
        } catch (Exception e) {
            throw new RuntimeException("Error while reset password");
        }
    }

    @Operation(summary = "Check token validity", description = "Check if the provided JWT token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "400", description = "Token is invalid or expired")
    })
    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @Transactional
    public ResponseEntity<?> test(@RequestParam(name = "token") String token) {
        try {
            return ResponseEntity.ok(authService.checkToken(token));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Get user data", description = "Retrieve user data based on the provided JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token")
    })
    @RequestMapping(value = "/loginResp", method = RequestMethod.GET)
    @Transactional
    public ResponseEntity<?> getUserData(@RequestParam(name = "token") String token) {
        try {
            return ResponseEntity.ok(authService.getUserData(token));
        }catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @RequestMapping(value = "/menu", method = RequestMethod.GET)
    @Transactional(readOnly = true)
    public ResponseEntity<?> getData(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = JwtTokenUtil.extractToken(authorizationHeader);
            return ResponseEntity.ok(authService.getUserMenusV2(token));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
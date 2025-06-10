package com.example.referral.controller;

import com.example.referral.dto.LoginRequest;
import com.example.referral.dto.SignupRequest;
import com.example.referral.model.User;
import com.example.referral.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided email and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        log.info("trying to signup: {}", request.getEmail() );
        User user = userService.signup(request.getEmail(), request.getPassword(), request.getName(), request.getReferralCode());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token along with user details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Map<String, Object> response = userService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Authentication failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/referral-code")
    @Operation(summary = "Get user's referral code", description = "Retrieves the referral code for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Referral code retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReferralCodeResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getReferralCode(@Parameter(hidden = true) @RequestAttribute("user") User user) {
        try {
            String referralCode = userService.getReferralCode(user);
            return ResponseEntity.ok(new ReferralCodeResponse(referralCode));
        } catch (Exception e) {
            log.error("Failed to get referral code: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get referral code");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Response classes
    public static class ReferralCodeResponse {
        private String referralCode;

        public ReferralCodeResponse(String referralCode) {
            this.referralCode = referralCode;
        }

        public String getReferralCode() { return referralCode; }
        public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    }
} 
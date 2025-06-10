package com.example.referral.controller;

import com.example.referral.model.Profile;
import com.example.referral.model.User;
import com.example.referral.service.ProfileService;
import com.example.referral.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profile", description = "User profile management APIs")
@Slf4j
public class ProfileController {

    private final ProfileService profileService;
    private final UserService userService;

    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @PatchMapping
    @Operation(summary = "Update user profile", description = "Updates the profile information for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = Profile.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {
        try {
            log.info("Updating profile for user: {}", authentication.getName());
            User user = userService.findByEmail(authentication.getName());
            Profile profile = profileService.createOrUpdateProfile(
                user,
                request.getStreet(),
                request.getCity(),
                request.getState(),
                request.getZipCode(),
                request.getPhoneNumber(),
                request.getDob()
            );

            return ResponseEntity.ok(Map.of(
                "message", "Profile updated",
                "isComplete", profile.isComplete()
            ));
        } catch (Exception e) {
            log.error("Error updating profile for user: {} - {}", authentication.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update profile",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping
    @Operation(summary = "Get user profile", description = "Retrieves the profile information for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = Profile.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            log.info("Getting profile for user: {}", authentication.getName());
            User user = userService.findByEmail(authentication.getName());
            Profile profile = profileService.getProfile(user);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error getting profile for user: {} - {}", authentication.getName(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to get profile",
                "message", e.getMessage()
            ));
        }
    }

    @Data
    public static class ProfileUpdateRequest {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String phoneNumber;
        private LocalDate dob;
    }
} 
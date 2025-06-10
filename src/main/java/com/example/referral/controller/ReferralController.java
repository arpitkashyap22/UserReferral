package com.example.referral.controller;

import com.example.referral.model.Referral;
import com.example.referral.model.User;
import com.example.referral.service.ReferralService;
import com.example.referral.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/referrals")
@Tag(name = "Referral", description = "Referral management APIs")
public class ReferralController {

    private final ReferralService referralService;
    private final UserService userService;

    public ReferralController(ReferralService referralService, UserService userService) {
        this.referralService = referralService;
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new referral", description = "Creates a new referral for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Referral created successfully",
            content = @Content(schema = @Schema(implementation = Referral.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> createReferral(
            Authentication authentication,
            @RequestBody ReferralRequest request) {
        User user = userService.findByEmail(authentication.getName());
        Referral referral = referralService.createReferral(user, request.getReferredEmail());
        return ResponseEntity.ok(referral);
    }

    @GetMapping
    @Operation(summary = "Get user's referrals", description = "Retrieves all referrals for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Referrals retrieved successfully",
            content = @Content(schema = @Schema(implementation = Referral.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getReferrals(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<Referral> referrals = referralService.getReferrals(user);
        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedReferrals(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        List<Referral> referrals = referralService.getReferralsByReferee(user);
        return ResponseEntity.ok(referrals);
    }

    @PostMapping("/{referralId}/complete")
    @Operation(summary = "Complete a referral", description = "Marks a referral as completed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Referral completed successfully",
            content = @Content(schema = @Schema(implementation = Referral.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Referral not found")
    })
    public ResponseEntity<?> completeReferral(
            Authentication authentication,
            @PathVariable Long referralId) {
        User user = userService.findByEmail(authentication.getName());
        Referral referral = referralService.completeReferral(user, referralId);
        return ResponseEntity.ok(referral);
    }

    @Data
    public static class ReferralRequest {
        private String referredEmail;
    }
} 
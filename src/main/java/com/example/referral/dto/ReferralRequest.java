package com.example.referral.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ReferralRequest {
    @NotBlank(message = "Referred email is required")
    @Email(message = "Invalid email format")
    private String referredEmail;

    // Getters and setters
    public String getReferredEmail() { return referredEmail; }
    public void setReferredEmail(String referredEmail) { this.referredEmail = referredEmail; }
} 
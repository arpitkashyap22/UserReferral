package com.example.referral.service;

import com.example.referral.model.Profile;
import com.example.referral.model.Referral;
import com.example.referral.model.User;
import com.example.referral.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ReferralService referralService;
    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    public ProfileService(ProfileRepository profileRepository, ReferralService referralService) {
        this.profileRepository = profileRepository;
        this.referralService = referralService;
    }

    @Transactional
    public Profile createOrUpdateProfile(User user, String street, String city, String state,
                                       String zipCode, String phoneNumber, LocalDate dob) {
        Profile profile = profileRepository.findByUser(user)
                .orElse(Profile.builder().user(user).build());

        profile.setStreet(street);
        profile.setCity(city);
        profile.setState(state);
        profile.setZipCode(zipCode);
        profile.setPhoneNumber(phoneNumber);
        profile.setDob(dob);

        Profile savedProfile = profileRepository.save(profile);

        // If profile is complete, trigger async referral processing
        if (savedProfile.isComplete()) {
            CompletableFuture.runAsync(() -> {
                try {
                    processPendingReferrals(user);
                } catch (Exception e) {
                    log.warn("Failed to process pending referrals for user {}: {}", user.getEmail(), e.getMessage());
                }
            });
        }

        return savedProfile;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processPendingReferrals(User user) {
        try {
            // Find all pending referrals where this user is the referee
            List<Referral> pendingReferrals = referralService.getReferralsByReferee(user).stream()
                    .filter(referral -> referral.getStatus() == Referral.Status.PENDING)
                    .collect(Collectors.toList());

            for (Referral referral : pendingReferrals) {
                try {
                    referralService.completeReferral(user, referral.getId());
                } catch (Exception e) {
                    log.warn("Failed to complete referral {} for user {}: {}", 
                        referral.getId(), user.getEmail(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Error checking referrals for user {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public Profile getProfile(User user) {
        return profileRepository.findByUser(user)
                .orElse(Profile.builder()
                        .user(user)
                        .build());
    }
}
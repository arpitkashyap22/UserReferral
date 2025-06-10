package com.example.referral.service;

import com.example.referral.model.Profile;
import com.example.referral.model.User;
import com.example.referral.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ReferralService referralService;

    @InjectMocks
    private ProfileService profileService;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setReferralCode("TEST123");
        testUser.setPassword("password123");

        testProfile = Profile.builder()
                .user(testUser)
                .street("123 Main St")
                .city("Test City")
                .state("Test State")
                .zipCode("12345")
                .phoneNumber("1234567890")
                .dob(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createOrUpdateProfile_Success() {
        when(profileRepository.findByUser(any(User.class))).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        Profile updatedProfile = profileService.createOrUpdateProfile(
                testUser,
                "123 Main St",
                "Test City",
                "Test State",
                "12345",
                "1234567890",
                LocalDate.of(1990, 1, 1)
        );

        assertNotNull(updatedProfile);
        assertEquals("123 Main St", updatedProfile.getStreet());
        assertEquals("Test City", updatedProfile.getCity());
        assertEquals("Test State", updatedProfile.getState());
        assertEquals("12345", updatedProfile.getZipCode());
        assertEquals("1234567890", updatedProfile.getPhoneNumber());
        assertEquals(LocalDate.of(1990, 1, 1), updatedProfile.getDob());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void createOrUpdateProfile_UpdateExisting() {
        when(profileRepository.findByUser(any(User.class))).thenReturn(Optional.of(testProfile));
        when(profileRepository.save(any(Profile.class))).thenReturn(testProfile);

        Profile updatedProfile = profileService.createOrUpdateProfile(
                testUser,
                "456 New St",
                "New City",
                "New State",
                "54321",
                "0987654321",
                LocalDate.of(1991, 2, 2)
        );

        assertNotNull(updatedProfile);
        assertEquals("456 New St", updatedProfile.getStreet());
        assertEquals("New City", updatedProfile.getCity());
        assertEquals("New State", updatedProfile.getState());
        assertEquals("54321", updatedProfile.getZipCode());
        assertEquals("0987654321", updatedProfile.getPhoneNumber());
        assertEquals(LocalDate.of(1991, 2, 2), updatedProfile.getDob());
        verify(profileRepository).save(any(Profile.class));
    }

    @Test
    void getProfile_Success() {
        when(profileRepository.findByUser(any(User.class))).thenReturn(Optional.of(testProfile));

        Profile profile = profileService.getProfile(testUser);

        assertNotNull(profile);
        assertEquals(testUser, profile.getUser());
        assertEquals("123 Main St", profile.getStreet());
    }

    @Test
    void getProfile_NotFound() {
        when(profileRepository.findByUser(any(User.class))).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            profileService.getProfile(testUser)
        );
    }
} 
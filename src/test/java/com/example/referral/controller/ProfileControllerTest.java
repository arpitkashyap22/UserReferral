package com.example.referral.controller;

import com.example.referral.model.Profile;
import com.example.referral.model.User;
import com.example.referral.service.ProfileService;
import com.example.referral.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setReferralCode("TEST123");

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
    @WithMockUser(username = "test@example.com")
    void updateProfile_Success() throws Exception {
        ProfileController.ProfileUpdateRequest request = new ProfileController.ProfileUpdateRequest();
        request.setStreet("456 New St");
        request.setCity("New City");
        request.setState("New State");
        request.setZipCode("54321");
        request.setPhoneNumber("0987654321");
        request.setDob(LocalDate.of(1991, 2, 2));

        when(userService.findByEmail(anyString())).thenReturn(testUser);
        when(profileService.createOrUpdateProfile(any(User.class), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(testProfile);

        mockMvc.perform(patch("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated"))
                .andExpect(jsonPath("$.isComplete").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfile_UserNotFound() throws Exception {
        ProfileController.ProfileUpdateRequest request = new ProfileController.ProfileUpdateRequest();
        request.setStreet("456 New St");

        when(userService.findByEmail(anyString()))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(patch("/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getProfile_Success() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(testUser);
        when(profileService.getProfile(any(User.class))).thenReturn(testProfile);

        mockMvc.perform(get("/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("Test City"))
                .andExpect(jsonPath("$.state").value("Test State"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getProfile_NotFound() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(testUser);
        when(profileService.getProfile(any(User.class)))
                .thenThrow(new RuntimeException("Profile not found"));

        mockMvc.perform(get("/profiles"))
                .andExpect(status().isNotFound());
    }
} 
package com.example.referral.controller;

import com.example.referral.model.Referral;
import com.example.referral.model.User;
import com.example.referral.service.ReferralService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReferralController.class)
class ReferralControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReferralService referralService;

    @MockBean
    private UserService userService;

    private User referrer;
    private User referee;
    private Referral referral;

    @BeforeEach
    void setUp() {
        referrer = new User();
        referrer.setEmail("referrer@example.com");
        referrer.setName("Referrer");
        referrer.setReferralCode("REF123");

        referee = new User();
        referee.setEmail("referee@example.com");
        referee.setName("Referee");
        referee.setReferralCode("REF456");

        referral = new Referral();
        referral.setReferrer(referrer);
        referral.setReferee(referee);
        referral.setStatus(Referral.Status.PENDING);
    }

    @Test
    @WithMockUser(username = "referrer@example.com")
    void createReferral_Success() throws Exception {
        ReferralController.CreateReferralRequest request = new ReferralController.CreateReferralRequest();
        request.setRefereeEmail("referee@example.com");

        when(userService.findByEmail("referrer@example.com")).thenReturn(referrer);
        when(userService.findByEmail("referee@example.com")).thenReturn(referee);
        when(referralService.createReferral(any(User.class), any(User.class))).thenReturn(referral);

        mockMvc.perform(post("/referrals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "referrer@example.com")
    void createReferral_RefereeNotFound() throws Exception {
        ReferralController.CreateReferralRequest request = new ReferralController.CreateReferralRequest();
        request.setRefereeEmail("nonexistent@example.com");

        when(userService.findByEmail("referrer@example.com")).thenReturn(referrer);
        when(userService.findByEmail("nonexistent@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/referrals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "referrer@example.com")
    void getReferrals_Success() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(referrer);
        when(referralService.getReferralsByReferrer(any(User.class)))
                .thenReturn(Arrays.asList(referral));

        mockMvc.perform(get("/referrals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "referee@example.com")
    void getReceivedReferrals_Success() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(referee);
        when(referralService.getReferralsByReferee(any(User.class)))
                .thenReturn(Arrays.asList(referral));

        mockMvc.perform(get("/referrals/received"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "referee@example.com")
    void completeReferral_Success() throws Exception {
        when(userService.findByEmail(anyString())).thenReturn(referee);

        mockMvc.perform(post("/referrals/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Referral completed successfully"));
    }
} 
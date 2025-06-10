package com.example.referral.controller;

import com.example.referral.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReferralReport_Success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalReferrals", 10);
        metrics.put("successfulReferrals", 5);
        report.put("metrics", metrics);

        when(reportService.generateReferralReport()).thenReturn(report);

        mockMvc.perform(get("/reports/referrals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics.totalReferrals").value(10))
                .andExpect(jsonPath("$.metrics.successfulReferrals").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getCsvReport_Success() throws Exception {
        String csvContent = "ReferrerEmail,RefereeEmail,ReferralCode,Status,CreatedAt\n" +
                "user1@example.com,user2@example.com,REF123,COMPLETED,2024-03-20\n";

        when(reportService.generateCsvReport()).thenReturn(csvContent);

        mockMvc.perform(get("/reports/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=referrals.csv"))
                .andExpect(content().string(csvContent));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getReferralReport_Unauthorized() throws Exception {
        mockMvc.perform(get("/reports/referrals"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCsvReport_Unauthorized() throws Exception {
        mockMvc.perform(get("/reports/csv"))
                .andExpect(status().isForbidden());
    }
} 
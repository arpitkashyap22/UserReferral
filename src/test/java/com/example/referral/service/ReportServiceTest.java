package com.example.referral.service;

import com.example.referral.model.Referral;
import com.example.referral.model.User;
import com.example.referral.repository.ReferralRepository;
import com.example.referral.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReferralRepository referralRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReportService reportService;

    private User user1;
    private User user2;
    private Referral referral1;
    private Referral referral2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setName("User 1");
        user1.setReferralCode("REF123");

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setName("User 2");
        user2.setReferralCode("REF456");

        referral1 = new Referral();
        referral1.setReferrer(user1);
        referral1.setReferee(user2);
        referral1.setStatus(Referral.ReferralStatus.COMPLETED);

        referral2 = new Referral();
        referral2.setReferrer(user2);
        referral2.setReferee(user1);
        referral2.setStatus(Referral.ReferralStatus.PENDING);
    }

    @Test
    void generateReferralReport_Success() {
        when(referralRepository.findAll()).thenReturn(Arrays.asList(referral1, referral2));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        Map<String, Object> report = reportService.generateReferralReport();

        assertNotNull(report);
        assertTrue(report.containsKey("referrals"));
        assertTrue(report.containsKey("metrics"));

        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) report.get("metrics");
        assertEquals(2L, metrics.get("totalReferrals"));
        assertEquals(1L, metrics.get("successfulReferrals"));
        assertTrue(metrics.containsKey("referralsPerUser"));
    }

    @Test
    void generateCsvReport_Success() throws Exception {
        when(referralRepository.findAll()).thenReturn(Arrays.asList(referral1, referral2));

        String csvReport = reportService.generateCsvReport();

        assertNotNull(csvReport);
        assertTrue(csvReport.contains("ReferrerEmail"));
        assertTrue(csvReport.contains("RefereeEmail"));
        assertTrue(csvReport.contains("ReferralCode"));
        assertTrue(csvReport.contains("Status"));
        assertTrue(csvReport.contains("CreatedAt"));
    }
} 
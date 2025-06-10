package com.example.referral.service;

import com.example.referral.model.Referral;
import com.example.referral.model.User;
import com.example.referral.repository.ReferralRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferralServiceTest {

    @Mock
    private ReferralRepository referralRepository;

    @InjectMocks
    private ReferralService referralService;

    private User referrer;
    private User referee;
    private Referral referral;

    @BeforeEach
    void setUp() {
        // Create referrer
        referrer = new User();
        referrer.setEmail("referrer@example.com");
        referrer.setName("Referrer");
        referrer.setReferralCode("REF123");
        referrer.setPassword("password123");

        // Create referee
        referee = new User();
        referee.setEmail("referee@example.com");
        referee.setName("Referee");
        referee.setReferralCode("REF456");
        referee.setPassword("password123");

        // Create referral
        referral = new Referral();
        referral.setReferrer(referrer);
        referral.setReferee(referee);
        referral.setStatus(Referral.ReferralStatus.PENDING);
    }

    @Test
    void createReferral_Success() {
        when(referralRepository.save(any(Referral.class))).thenReturn(referral);

        Referral createdReferral = referralService.createReferral(referrer, referee);

        assertNotNull(createdReferral);
        assertEquals(referrer, createdReferral.getReferrer());
        assertEquals(referee, createdReferral.getReferee());
        assertEquals(Referral.ReferralStatus.PENDING, createdReferral.getStatus());
        verify(referralRepository).save(any(Referral.class));
    }

    @Test
    void getReferralsByReferrer_Success() {
        when(referralRepository.findByReferrer(any(User.class)))
                .thenReturn(Arrays.asList(referral));

        List<Referral> referrals = referralService.getReferralsByReferrer(referrer);

        assertNotNull(referrals);
        assertEquals(1, referrals.size());
        assertEquals(referrer, referrals.get(0).getReferrer());
    }

    @Test
    void getReferralsByReferee_Success() {
        when(referralRepository.findByReferee(any(User.class)))
                .thenReturn(Arrays.asList(referral));

        List<Referral> referrals = referralService.getReferralsByReferee(referee);

        assertNotNull(referrals);
        assertEquals(1, referrals.size());
        assertEquals(referee, referrals.get(0).getReferee());
    }

    @Test
    void completeReferral_Success() {
        when(referralRepository.findById(anyLong())).thenReturn(Optional.of(referral));
        when(referralRepository.save(any(Referral.class))).thenReturn(referral);

        referralService.completeReferral(1L);

        assertEquals(Referral.Status.COMPLETED, referral.getStatus());
        verify(referralRepository).save(referral);
    }

    @Test
    void completeReferral_NotFound() {
        when(referralRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            referralService.completeReferral(1L)
        );
    }
} 
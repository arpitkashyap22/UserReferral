package com.example.referral.service;

import com.example.referral.model.Referral;
import com.example.referral.model.User;
import com.example.referral.repository.ReferralRepository;
import com.example.referral.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReferralService {

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Referral createReferral(User referrer, String referredEmail) {
        User referee = userRepository.findByEmail(referredEmail)
                .orElseThrow(() -> new RuntimeException("Referred user not found"));

        if (referralRepository.existsByReferrerAndReferee(referrer, referee)) {
            throw new RuntimeException("Referral already exists");
        }

        Referral referral = new Referral();
        referral.setReferrer(referrer);
        referral.setReferee(referee);
        referral.setStatus(Referral.Status.PENDING);
        return referralRepository.save(referral);
    }

    @Transactional
    public Referral createReferral(String referrerCode, User referred) {
        User referee = userRepository.findByReferralCode(referrerCode)
                .orElseThrow(() -> new RuntimeException("Referred user not found"));

        if (referralRepository.existsByReferrerAndReferee(referred, referee)) {
            throw new RuntimeException("Referral already exists");
        }

        Referral referral = new Referral();
        referral.setReferrer(referred);
        referral.setReferee(referee);
        referral.setStatus(Referral.Status.PENDING);
        return referralRepository.save(referral);
    }




    public List<Referral> getReferrals(User user) {
        return referralRepository.findByReferrer(user);
    }

    @Transactional
    public Referral completeReferral(User user, Long referralId) {
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new RuntimeException("Referral not found"));

        if (!referral.getReferrer().equals(user) && !referral.getReferee().equals(user)) {
            throw new RuntimeException("Unauthorized to complete this referral");
        }

        referral.setStatus(Referral.Status.COMPLETED);
        return referralRepository.save(referral);
    }

    public List<Referral> getReferralsByReferrer(User referrer) {
        return referralRepository.findByReferrer(referrer);
    }

    public List<Referral> getReferralsByReferee(User referee) {
        return referralRepository.findByReferee(referee);
    }

    public List<Referral> getAllReferrals() {
        return referralRepository.findAll();
    }
}
package com.example.referral.repository;

import com.example.referral.model.Referral;
import com.example.referral.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByReferrer(User referrer);
    List<Referral> findByReferee(User referee);
    List<Referral> findByReferrerEmail(String referrerEmail);
    List<Referral> findByRefereeEmail(String refereeEmail);
    boolean existsByReferrerAndReferee(User referrer, User referee);
} 
package com.example.referral.repository;

import com.example.referral.model.Profile;
import com.example.referral.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(User user);
    boolean existsByUser(User user);
} 
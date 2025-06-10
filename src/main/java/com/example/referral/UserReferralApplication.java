package com.example.referral;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserReferralApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserReferralApplication.class, args);
    }
} 
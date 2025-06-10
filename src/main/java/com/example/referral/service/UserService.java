package com.example.referral.service;

import com.example.referral.model.User;
import com.example.referral.repository.UserRepository;
import com.example.referral.security.JwtTokenUtil;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int REFERRAL_CODE_LENGTH = 6;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ReferralService referralService;


    @Transactional
    @Timed(value = "user.registration.time", description = "Time taken to register a new user")
    public User signup(String email, String password, String name, String referralCode) {
        logger.info("Starting signup process for email: {}", email);
        try {
            User referrer = null;
            if (referralCode != null && !referralCode.trim().isEmpty()) {
                logger.debug("Processing referral code: {}", referralCode);
                try {
                    referrer = findByReferralCode(referralCode);
                    logger.info("Found referrer with email: {}", referrer.getEmail());
                } catch (Exception e) {
                    logger.error("Error finding referrer with code {}: {}", referralCode, e.getMessage());
                    throw new RuntimeException("Invalid referral code: " + referralCode);
                }
            }

            logger.debug("Checking if email already exists: {}", email);
            if (userRepository.existsByEmail(email)) {
                logger.warn("Registration failed: Email already exists: {}", email);
                throw new RuntimeException("Email already exists");
            }

            String generatedReferralCode = generateUniqueReferralCode();
            logger.debug("Generated referral code: {}", generatedReferralCode);

            logger.debug("Creating new user with email: {}", email);
            User user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .name(name)
                    .referralCode(generatedReferralCode)
                    .referralPoints(0)
                    .build();

            logger.debug("Saving user to database");
            User savedUser = userRepository.save(user);
            logger.info("Successfully saved user with ID: {}", savedUser.getId());

            if (referrer != null) {
                logger.debug("Creating referral for user: {} referred by: {}", email, referrer.getEmail());
                try {
                    referralService.createReferral(referrer, savedUser.getEmail());
                    logger.info("Successfully created referral for user: {} referred by {}", email, referrer.getEmail());
                } catch (Exception e) {
                    logger.error("Error creating referral: {}", e.getMessage());
                    throw new RuntimeException("Failed to create referral: " + e.getMessage());
                }
            } else {
                logger.info("No referral code provided for user: {}", email);
            }

            return savedUser;
        } catch (Exception e) {
            logger.error("Error during signup for email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Signup failed: " + e.getMessage());
        }
    }

    @Timed(value = "user.login.time", description = "Time taken to login a user")
    public Map<String, Object> login(String email, String password) {
        logger.debug("Attempting to login user: {}", email);
        try {
            // Check if user exists first
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new IllegalArgumentException("Invalid email or password");
                });
            
            logger.debug("Found user with email: {}", email);
            
            // Authenticate user
            logger.debug("Authenticating user with email: {}", email);
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            
            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            logger.debug("Successfully authenticated user: {}", email);
            
            // Generate token
            String token = jwtTokenUtil.generateToken(userDetails);
            logger.debug("Generated token for user: {}", email);
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            
            Map<String, Object> userDetailsMap = new HashMap<>();
            userDetailsMap.put("id", user.getId());
            userDetailsMap.put("email", user.getEmail());
            userDetailsMap.put("name", user.getName());
            userDetailsMap.put("referralCode", user.getReferralCode());
            userDetailsMap.put("referralPoints", user.getReferralPoints());
            
            response.put("user", userDetailsMap);
            logger.info("Successfully logged in user: {}", email);
            return response;
            
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for user: {}", email);
            throw new IllegalArgumentException("Invalid email or password");
        } catch (IllegalArgumentException e) {
            logger.error("Login error for user: {} - {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {} - {}", email, e.getMessage(), e);
            throw new RuntimeException("Error during login: " + e.getMessage());
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Timed(value = "user.find.by.referral.time", description = "Time taken to find a user by referral code")
    public User findByReferralCode(String referralCode) {
        logger.debug("Finding user by referral code: {}", referralCode);
        return userRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> {
                    logger.warn("User not found with referral code: {}", referralCode);
                    return new IllegalArgumentException("Invalid referral code");
                });
    }

    public String getReferralCode(User user) {
        logger.debug("Getting referral code for user: {}", user.getEmail());
        return user.getReferralCode();
    }

    private String generateUniqueReferralCode() {
        Random random = new Random();
        StringBuilder code;
        do {
            code = new StringBuilder(REFERRAL_CODE_LENGTH);
            for (int i = 0; i < REFERRAL_CODE_LENGTH; i++) {
                code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
        } while (userRepository.existsByReferralCode(code.toString()));
        return code.toString();
    }
} 
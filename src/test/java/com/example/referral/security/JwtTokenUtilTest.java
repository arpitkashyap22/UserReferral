package com.example.referral.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private UserDetails userDetails;
    private static final String SECRET_KEY = "testSecretKey1234567890123456789012345678901234567890";
    private static final long EXPIRATION_TIME = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", EXPIRATION_TIME);

        userDetails = new User("test@example.com", "password", new ArrayList<>());
    }

    @Test
    void generateToken_Success() {
        String token = jwtTokenUtil.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.length() > 0);

        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();

        assertEquals("test@example.com", claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void validateToken_Success() {
        String token = jwtTokenUtil.generateToken(userDetails);
        assertTrue(jwtTokenUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_InvalidUser() {
        String token = jwtTokenUtil.generateToken(userDetails);
        UserDetails differentUser = new User("different@example.com", "password", new ArrayList<>());
        assertFalse(jwtTokenUtil.validateToken(token, differentUser));
    }

    @Test
    void validateToken_ExpiredToken() {
        // Set a very short expiration time
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", 1L);
        String token = jwtTokenUtil.generateToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(jwtTokenUtil.validateToken(token, userDetails));
    }

    @Test
    void getUsernameFromToken_Success() {
        String token = jwtTokenUtil.generateToken(userDetails);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        assertEquals("test@example.com", username);
    }

    @Test
    void getUsernameFromToken_InvalidToken() {
        assertThrows(Exception.class, () -> 
            jwtTokenUtil.getUsernameFromToken("invalid.token.here")
        );
    }
} 
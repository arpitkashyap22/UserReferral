package com.example.referral.service;

import com.example.referral.model.User;
import com.example.referral.repository.UserRepository;
import com.example.referral.security.CustomUserDetailsService;
import com.example.referral.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .referralCode("TEST123")
                .build();
    }

    @Test
    void signup_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByReferralCode(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registeredUser = userService.signup("test@example.com", "password123", "Test User", null);

        assertNotNull(registeredUser);
        assertEquals("test@example.com", registeredUser.getEmail());
        assertEquals("Test User", registeredUser.getName());
        assertNotNull(registeredUser.getReferralCode());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signup_DuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
            userService.signup("test@example.com", "password123", "Test User", null)
        );
    }

    @Test
    void authenticate_Success() {
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtTokenUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        String token = userService.authenticate("test@example.com", "password123");

        assertNotNull(token);
        assertEquals("jwt-token", token);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void findByEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        User foundUser = userService.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            userService.findByEmail("nonexistent@example.com")
        );
    }

    @Test
    void findByReferralCode_Success() {
        when(userRepository.findByReferralCode(anyString())).thenReturn(Optional.of(testUser));

        User foundUser = userService.findByReferralCode("TEST123");

        assertNotNull(foundUser);
        assertEquals("TEST123", foundUser.getReferralCode());
    }

    @Test
    void findByReferralCode_NotFound() {
        when(userRepository.findByReferralCode(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            userService.findByReferralCode("INVALID")
        );
    }
} 
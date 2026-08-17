package com.backend.modulo.security;

import com.backend.modulo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private UserDetails userDetails;

    @BeforeEach
    public void setUp() {
        // Create a mock user
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encoded_password")
                .role(User.Role.USER)
                .build();
        userDetails = user;
    }

    @Test
    public void testGenerateAndValidateToken() {
        // Generate token
        String token = jwtTokenProvider.generateToken(userDetails);
        assertNotNull(token);

        // Validate token
        Boolean isValid = jwtTokenProvider.validateToken(token, userDetails);
        assertTrue(isValid, "Token should be valid");

        // Extract username from token
        String extractedUsername = jwtTokenProvider.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername, "Username should match");
    }

    @Test
    public void testGenerateDeviceToken() {
        String deviceToken = jwtTokenProvider.generateDeviceToken(userDetails);
        assertNotNull(deviceToken);

        // Device token should also validate
        Boolean isValid = jwtTokenProvider.validateToken(deviceToken, userDetails);
        assertTrue(isValid, "Device token should be valid");
    }
}

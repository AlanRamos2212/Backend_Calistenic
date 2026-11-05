package com.backend.modulo.security;

import com.backend.modulo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.JwtException;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for JWT rules without a Spring context or external services. */
class JwtTokenProviderTest {

    private static final String SECRET =
            "CalistenicTrackUnitTestSecretKeyMustBeLongEnoughForHS256";

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider(SECRET);
        setField(jwtTokenProvider, "jwtExpirationMs", 3_600_000L);
        setField(jwtTokenProvider, "deviceJwtExpirationMs", 900_000L);

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
    void validTokenIsGeneratedAndValidated() {
        String token = jwtTokenProvider.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token, userDetails));
        assertEquals(userDetails.getUsername(), jwtTokenProvider.extractUsername(token));
    }

    @Test
    void tokenForAnotherUserIsRejected() {
        String token = jwtTokenProvider.generateToken(userDetails);
        User anotherUser = User.builder()
                .id(2L)
                .name("Another User")
                .email("another@example.com")
                .password("encoded_password")
                .role(User.Role.USER)
                .build();

        assertFalse(jwtTokenProvider.validateToken(token, anotherUser));
    }

    @Test
    void malformedTokenRaisesJwtError() {
        assertThrows(JwtException.class,
                () -> jwtTokenProvider.validateToken("not-a-jwt", userDetails));
    }

    @Test
    void deviceTokenIsValidAndContainsDeviceUseClaim() {
        String deviceToken = jwtTokenProvider.generateDeviceToken(userDetails);

        assertNotNull(deviceToken);
        assertTrue(jwtTokenProvider.validateToken(deviceToken, userDetails));
        assertEquals("device", jwtTokenProvider.extractAllClaims(deviceToken).get("token_use"));
    }

    private static void setField(Object target, String name, long value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.setLong(target, value);
    }
}

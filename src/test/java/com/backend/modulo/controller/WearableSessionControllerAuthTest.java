package com.backend.modulo.controller;

import com.backend.modulo.dto.AuthResponse;
import com.backend.modulo.dto.LoginRequest;
import com.backend.modulo.dto.RegisterRequest;
import com.backend.modulo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class WearableSessionControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String validToken;
    private String testEmail = "wearable-test@example.com";

    @BeforeEach
    public void setUp() throws Exception {
        // Clean up any existing user
        userRepository.deleteByEmail(testEmail);

        // Register a new user
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Wearable Test User")
                .email(testEmail)
                .password("TestPassword123!")
                .build();

        String registerJson = objectMapper.writeValueAsString(registerRequest);

        String registerResponse = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(registerResponse, AuthResponse.class);
        validToken = authResponse.getToken();
    }

    @Test
    public void testWearableSessionStatsWithValidToken() throws Exception {
        mockMvc.perform(get("/api/wearable-sessions/stats")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginReturnsToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email(testEmail)
                .password("TestPassword123!")
                .build();

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void testRegistrationValidationExplainsRequiredFields() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .name("A")
                .email("invalid-email")
                .password("short")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("nombre")))
                .andExpect(jsonPath("$.message", containsString("contraseña")));
    }

    @Test
    public void testDuplicateRegistrationExplainsEmailConflict() throws Exception {
        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .name("Another User")
                .email(testEmail)
                .password("TestPassword123!")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("correo")));
    }

    @Test
    public void testWearableSessionStatsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/wearable-sessions/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testWearableSessionStatsWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/wearable-sessions/stats")
                .header("Authorization", "Bearer invalid_token_here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeviceTokenEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/device-token")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }
}

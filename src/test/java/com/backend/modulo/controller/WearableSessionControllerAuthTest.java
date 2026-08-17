package com.backend.modulo.controller;

import com.backend.modulo.dto.AuthResponse;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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

        System.out.println("Valid token obtained: " + validToken);
    }

    @Test
    public void testWearableSessionStatsWithValidToken() throws Exception {
        mockMvc.perform(get("/api/wearable-sessions/stats")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()));
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
        String deviceTokenResponse = mockMvc.perform(post("/api/auth/device-token")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("Device token response: " + deviceTokenResponse);
    }
}

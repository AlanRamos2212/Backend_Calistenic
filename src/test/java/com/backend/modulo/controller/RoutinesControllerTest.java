package com.backend.modulo.controller;

import com.backend.modulo.dto.CreateRoutineRequest;
import com.backend.modulo.dto.LoginRequest;
import com.backend.modulo.dto.RegisterRequest;
import com.backend.modulo.entity.User;
import com.backend.modulo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoutinesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_EMAIL = "routines-user@example.com";
    private static final String ADMIN_EMAIL = "routines-admin@example.com";

    @Test
    @Order(1)
    @Transactional
    void listContainsSeedRoutinesWithExerciseCounts() throws Exception {
        String token = tokenFor(USER_EMAIL, false);
        mockMvc.perform(get("/api/routines").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].title").isNotEmpty())
                .andExpect(jsonPath("$[0].exerciseCount").isNumber());
    }

    @Test
    @Order(2)
    @Transactional
    void detailReturnsOrderedExercises() throws Exception {
        String token = tokenFor(USER_EMAIL, false);
        mockMvc.perform(get("/api/routines/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.exercises").isArray())
                .andExpect(jsonPath("$.exercises[0].position").value(1))
                .andExpect(jsonPath("$.exercises[0].sets").isNumber())
                .andExpect(jsonPath("$.exercises[0].restSeconds").isNumber());
    }

    @Test
    @Order(3)
    @Transactional
    void detailOfUnknownRoutineReturns404() throws Exception {
        String token = tokenFor(USER_EMAIL, false);
        mockMvc.perform(get("/api/routines/999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    @Transactional
    void createRejectsNonAdmin() throws Exception {
        String token = tokenFor(USER_EMAIL, false);
        mockMvc.perform(post("/api/routines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    @Transactional
    void adminCreatesAndDeletesRoutine() throws Exception {
        String token = tokenFor(ADMIN_EMAIL, true);
        String body = validCreateBody();

        String created = mockMvc.perform(post("/api/routines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.exerciseCount").value(2))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).path("id").asLong();

        mockMvc.perform(get("/api/routines/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises.length()").value(2))
                .andExpect(jsonPath("$.exercises[0].position").value(1))
                .andExpect(jsonPath("$.exercises[1].position").value(2));

        mockMvc.perform(delete("/api/routines/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/routines/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String tokenFor(String email, boolean admin) throws Exception {
        String password = "TestPassword123!";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .name("Routines Test")
                        .email(email)
                        .password(password)
                        .build())));

        if (admin) {
            User user = userRepository.findByEmail(email).orElseThrow();
            user.setRole(User.Role.ADMIN);
            userRepository.save(user);
        }

        String loginJson = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest.builder()
                        .email(email)
                        .password(password)
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(loginJson).isNotBlank();
        return objectMapper.readTree(loginJson).path("token").asText();
    }

    private String validCreateBody() throws Exception {
        CreateRoutineRequest request = new CreateRoutineRequest();
        request.setTitle("Cuenta de la Fase B");
        request.setSubtitle("Creada desde el test");
        request.setCategory("full_body");
        request.setLevel("principiante");
        request.setDurationMin(25);
        request.setEstimatedCalories(300);
        CreateRoutineRequest.ExerciseRequest first = new CreateRoutineRequest.ExerciseRequest();
        first.setExerciseId(1L);
        first.setSets(3);
        first.setReps(12);
        first.setRestSeconds(60);
        CreateRoutineRequest.ExerciseRequest second = new CreateRoutineRequest.ExerciseRequest();
        second.setExerciseId(4L);
        second.setSets(2);
        second.setReps(45);
        second.setRestSeconds(40);
        request.setExercises(List.of(first, second));
        return objectMapper.writeValueAsString(request);
    }
}

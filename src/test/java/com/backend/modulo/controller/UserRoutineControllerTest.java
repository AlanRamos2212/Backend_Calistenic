package com.backend.modulo.controller;

import com.backend.modulo.dto.CreateUserRoutineRequest;
import com.backend.modulo.dto.LoginRequest;
import com.backend.modulo.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserRoutineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String PASSWORD = "TestPassword123!";

    @Test
    void fullOwnerLifecycle() throws Exception {
        String token = tokenFor("user-routine-owner@example.com");
        String body = createBody();

        // Create: 201 with ordered exercises and joined media/prescription data.
        String created = mockMvc.perform(post("/api/user-routines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Mi rutina de prueba"))
                .andExpect(jsonPath("$.exercises.length()").value(2))
                .andExpect(jsonPath("$.exercises[0].position").value(1))
                .andExpect(jsonPath("$.exercises[0].sets").value(4))
                .andExpect(jsonPath("$.exercises[0].reps").value(8))
                .andExpect(jsonPath("$.exercises[0].restSeconds").value(90))
                .andExpect(jsonPath("$.exercises[1].position").value(2))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(created).path("id").asLong();

        // List contains the created card.
        mockMvc.perform(get("/api/user-routines").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].title").value("Mi rutina de prueba"))
                .andExpect(jsonPath("$[0].exerciseCount").value(2));

        // Unauthenticated request cannot list.
        mockMvc.perform(get("/api/user-routines"))
                .andExpect(status().is4xxClientError());

        ownerDelete(token, id);
    }

    @Test
    void otherUserCannotSeeNorDeleteForeignRoutine() throws Exception {
        String ownerToken = tokenFor("user-routine-owner2@example.com");
        String otherToken = tokenFor("user-routine-other@example.com");

        String created = mockMvc.perform(post("/api/user-routines")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).path("id").asLong();

        // Another user's routine reads as not found (no existence leak).
        mockMvc.perform(get("/api/user-routines/" + id)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // The other user's list does NOT include it.
        mockMvc.perform(get("/api/user-routines").header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // And cannot delete it either.
        mockMvc.perform(delete("/api/user-routines/" + id)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // Unknown routine -> 404 for the owner as well.
        mockMvc.perform(get("/api/user-routines/999999")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());

        // Owner deletes successfully.
        mockMvc.perform(delete("/api/user-routines/" + id)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void emptyExercisesFailValidation() throws Exception {
        String token = tokenFor("user-routine-validation@example.com");
        mockMvc.perform(post("/api/user-routines")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Sin ejercicios\"}"))
                .andExpect(status().isBadRequest());
    }

    private void ownerDelete(String token, long id) throws Exception {
        mockMvc.perform(delete("/api/user-routines/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/user-routines/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private String createBody() throws Exception {
        CreateUserRoutineRequest request = new CreateUserRoutineRequest();
        request.setTitle("Mi rutina de prueba");
        CreateUserRoutineRequest.ExerciseRequest first = new CreateUserRoutineRequest.ExerciseRequest();
        first.setExerciseId(2L); // Pull-ups
        first.setSets(4);
        first.setReps(8);
        first.setRestSeconds(90);
        CreateUserRoutineRequest.ExerciseRequest second = new CreateUserRoutineRequest.ExerciseRequest();
        second.setExerciseId(8L); // L-Sit, time-based (reps null)
        second.setSets(3);
        second.setReps(null);
        second.setRestSeconds(45);
        request.setExercises(List.of(first, second));
        return objectMapper.writeValueAsString(request);
    }

    private String tokenFor(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .name("User Routines Test")
                        .email(email)
                        .password(PASSWORD)
                        .build())))
                .andExpect(status().isOk());

        String loginJson = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequest.builder()
                        .email(email)
                        .password(PASSWORD)
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(loginJson).path("token").asText();
    }
}

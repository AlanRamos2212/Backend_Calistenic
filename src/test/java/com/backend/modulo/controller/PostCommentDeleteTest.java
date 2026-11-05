package com.backend.modulo.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PostCommentDeleteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authorDeletesOwnCommentAndOtherAuthorGets403() throws Exception {
        String authorToken = register("comment-author@example.com");
        String otherToken = register("comment-other@example.com");

        String postJson = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Post para pruebas de comentarios\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long postId = objectMapper.readTree(postJson).path("id").asLong();

        String authorComment = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Mi comentario\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Mi comentario"))
                .andReturn().getResponse().getContentAsString();
        long authorCommentId = objectMapper.readTree(authorComment).path("id").asLong();

        String otherComment = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Comentario ajeno\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long otherCommentId = objectMapper.readTree(otherComment).path("id").asLong();

        // A comment cannot be deleted via another post.
        mockMvc.perform(delete("/api/posts/" + (postId + 1) + "/comments/" + authorCommentId)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNotFound());

        // A non-author cannot delete someone else's comment.
        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + authorCommentId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());

        // Unknown comment id -> 404.
        mockMvc.perform(delete("/api/posts/" + postId + "/comments/999999")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNotFound());

        // The author deletes their own comment successfully.
        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + authorCommentId)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNoContent());
    }

    private String register(String email) throws Exception {
        String registerJson = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequest.builder()
                        .name("Comment Test")
                        .email(email)
                        .password("TestPassword123!")
                        .build())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // Register returns the session token directly; fall back to login.
        String token = objectMapper.readTree(registerJson).path("token").asText(null);
        if (token == null || token.isBlank()) {
            String loginJson = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(LoginRequest.builder()
                            .email(email)
                            .password("TestPassword123!")
                            .build())))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            token = objectMapper.readTree(loginJson).path("token").asText();
        }
        return token;
    }
}

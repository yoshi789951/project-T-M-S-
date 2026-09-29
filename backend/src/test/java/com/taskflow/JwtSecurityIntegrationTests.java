package com.taskflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullJwtAndRoleFlow() throws Exception {
        String userEmail = "student@example.com";
        String userPassword = "Password123!";

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Student User",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(userEmail, userPassword)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        JsonNode userAuth = signIn(userEmail, userPassword);
        String userToken = userAuth.get("token").asText();
        long userId = userAuth.get("userId").asLong();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").value(userToken));

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Forbidden User Project",
                                  "description": "A USER cannot create this",
                                  "status": "ACTIVE",
                                  "color": "#2563eb"
                                }
                                """))
                .andExpect(status().isForbidden());

        JsonNode adminAuth = signIn("admin@taskflow.local", "Admin123!");
        String adminToken = adminAuth.get("token").asText();

        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/users/{id}/role", userId)
                        .param("role", "MANAGER")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));

        // The existing token is accepted, but authorities are loaded from the database
        // on every request, so the role change applies immediately.
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Manager Project",
                                  "description": "Created after ADMIN promotion",
                                  "status": "ACTIVE",
                                  "color": "#2563eb"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Manager Project"));

        // Planning block is available to every authenticated user.
        MvcResult planResult = mockMvc.perform(post("/api/plans")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Manager delivery plan",
                                  "description": "Plan persisted in task_manager",
                                  "status": "ACTIVE",
                                  "priority": "HIGH",
                                  "startDate": "2026-09-29",
                                  "endDate": "2026-10-05",
                                  "progressPercent": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Manager delivery plan"))
                .andReturn();
        long planId = objectMapper.readTree(planResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/plans/{planId}/items", planId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Validate JWT flow",
                                  "description": "Plan checklist item",
                                  "status": "TODO",
                                  "sortOrder": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Validate JWT flow"));

        mockMvc.perform(get("/api/plans/{planId}/items", planId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Validate JWT flow"));

        mockMvc.perform(post("/api/reminders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Review plan",
                                  "message": "Check the planning block",
                                  "remindAt": "2026-10-01T10:00:00",
                                  "planId": %d,
                                  "repeatType": "NONE"
                                }
                                """.formatted(planId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/reminders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Review plan"));

        mockMvc.perform(patch("/api/admin/users/{id}/active", userId)
                        .param("active", "false")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode signIn(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}

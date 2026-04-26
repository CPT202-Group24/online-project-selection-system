package com.group24.projectselection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void toggleUserStatus_isReflectedInSubsequentUserListQuery() throws Exception {
        User targetUser = new User();
        targetUser.setEmail("target-user@test.com");
        targetUser.setPasswordHash("$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO");
        targetUser.setName("Target User");
        targetUser.setRole(User.Role.student);
        targetUser.setStatus(User.UserStatus.active);
        targetUser = userRepository.save(targetUser);

        mockMvc.perform(put("/api/admin/users/{id}/status", targetUser.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUser.getId()))
                .andExpect(jsonPath("$.status").value("disabled"));

        String userListJson = mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(userListJson);
        String updatedStatus = null;
        for (JsonNode node : root) {
            if (node.get("id").asLong() == targetUser.getId()) {
                updatedStatus = node.get("status").asText();
                break;
            }
        }

        assertThat(updatedStatus).isEqualTo("disabled");
    }
}

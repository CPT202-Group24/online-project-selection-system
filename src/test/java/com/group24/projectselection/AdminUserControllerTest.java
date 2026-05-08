package com.group24.projectselection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group24.projectselection.model.AuditLog;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.AuditLogRepository;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.AuditLogService;
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

    @Autowired
    private AuditLogRepository auditLogRepository;

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

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void changeRole_validRole_updatesTargetUserRole() throws Exception {
        User targetUser = new User();
        targetUser.setEmail("role-target@test.com");
        targetUser.setPasswordHash("$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO");
        targetUser.setName("Role Target");
        targetUser.setRole(User.Role.student);
        targetUser.setStatus(User.UserStatus.active);
        targetUser = userRepository.save(targetUser);

        mockMvc.perform(put("/api/admin/users/{id}/role", targetUser.getId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"role\":\"teacher\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUser.getId()))
                .andExpect(jsonPath("$.role").value("teacher"));

        User updated = userRepository.findById(targetUser.getId()).orElseThrow();
        assertThat(updated.getRole()).isEqualTo(User.Role.teacher);
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void changeRole_invalidRole_returnsBadRequest() throws Exception {
        User adminUser = userRepository.findByEmail("admin@test.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin@test.com");
            u.setPasswordHash("$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO");
            u.setName("Admin User");
            u.setRole(User.Role.admin);
            u.setStatus(User.UserStatus.active);
            return userRepository.save(u);
        });

        User targetUser = new User();
        targetUser.setEmail("invalid-role-target@test.com");
        targetUser.setPasswordHash("$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO");
        targetUser.setName("Invalid Role Target");
        targetUser.setRole(User.Role.student);
        targetUser.setStatus(User.UserStatus.active);
        targetUser = userRepository.save(targetUser);

        mockMvc.perform(put("/api/admin/users/{id}/role", targetUser.getId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"role\":\"not-a-role\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid role: not-a-role"));

        AuditLog latest = auditLogRepository.findAll().stream()
                .filter(log -> AuditLogService.ACTION_USER_ROLE_CHANGE_FAILED.equals(log.getActionType()))
                .findFirst()
                .orElse(null);
        assertThat(latest).isNotNull();
        assertThat(latest.getAdmin().getId()).isEqualTo(adminUser.getId());
        assertThat(latest.getEntityId()).isEqualTo(targetUser.getId());
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void changeRole_selfDowngrade_returnsBadRequest() throws Exception {
        User adminUser = userRepository.findByEmail("admin@test.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin@test.com");
            u.setPasswordHash("$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO");
            u.setName("Admin User");
            u.setRole(User.Role.admin);
            u.setStatus(User.UserStatus.active);
            return userRepository.save(u);
        });

        mockMvc.perform(put("/api/admin/users/{id}/role", adminUser.getId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"role\":\"teacher\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You cannot change your own role away from admin."));
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void toggleStatus_selfDisable_logsFailedAuditAction() throws Exception {
        User adminUser = userRepository.findByEmail("admin@test.com").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin@test.com");
            u.setPasswordHash("$2b$10$Sey1a6qg4tueIbitIt/R/eFrGlNEGuCdqBKQiUnWJX0o5TEaLDtsO");
            u.setName("Admin User");
            u.setRole(User.Role.admin);
            u.setStatus(User.UserStatus.active);
            return userRepository.save(u);
        });

        mockMvc.perform(put("/api/admin/users/{id}/status", adminUser.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("You cannot disable your own account."));

        AuditLog latest = auditLogRepository.findAll().stream()
                .filter(log -> AuditLogService.ACTION_USER_STATUS_TOGGLE_FAILED.equals(log.getActionType()))
                .findFirst()
                .orElse(null);
        assertThat(latest).isNotNull();
        assertThat(latest.getAdmin().getId()).isEqualTo(adminUser.getId());
        assertThat(latest.getEntityId()).isEqualTo(adminUser.getId());
    }
}

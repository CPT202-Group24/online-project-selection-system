package com.group24.projectselection.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests verifying the SecurityFilterChain configuration:
 * public endpoints, protected endpoints, and role-based access control.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Public endpoints accessible without authentication ──────────────

    @Test
    @DisplayName("GET /login is accessible without authentication")
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /register is accessible without authentication")
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /forgot-password is accessible without authentication")
    void forgotPasswordPage_isPublic() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /reset-password is accessible without authentication")
    void resetPasswordPage_isPublic() throws Exception {
        mockMvc.perform(get("/reset-password").param("token", "test-token"))
                .andExpect(status().isOk());
    }

    // ── Protected endpoints require authentication ──────────────────────

    @Test
    @DisplayName("GET /student/dashboard without auth redirects to login")
    void studentDashboard_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("GET /teacher/dashboard without auth redirects to login")
    void teacherDashboard_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("GET /admin/dashboard without auth redirects to login")
    void adminDashboard_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ── Role-based access control ───────────────────────────────────────

    @Test
    @DisplayName("Student cannot access /admin/** endpoints")
    @WithMockUser(authorities = "student")
    void student_cannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Teacher cannot access /admin/** endpoints")
    @WithMockUser(authorities = "teacher")
    void teacher_cannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can access /admin/** endpoints")
    @WithMockUser(authorities = "admin")
    void admin_canAccessAdminEndpoints() throws Exception {
        // The admin dashboard may return 200 or 500 depending on controller logic,
        // but it must NOT return 403 (forbidden) or 302 to login.
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Student cannot access /teacher/** endpoints")
    @WithMockUser(authorities = "student")
    void student_cannotAccessTeacherEndpoints() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Teacher can access /teacher/** endpoints")
    @WithMockUser(authorities = "teacher")
    void teacher_canAccessTeacherEndpoints() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Admin can access /teacher/** endpoints")
    @WithMockUser(authorities = "admin")
    void admin_canAccessTeacherEndpoints() throws Exception {
        mockMvc.perform(get("/teacher/dashboard"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Teacher cannot access /student/** endpoints")
    @WithMockUser(authorities = "teacher")
    void teacher_cannotAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin cannot access /student/** endpoints")
    @WithMockUser(authorities = "admin")
    void admin_cannotAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student can access /student/** endpoints")
    @WithMockUser(authorities = "student")
    void student_canAccessStudentEndpoints() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is2xxSuccessful());
    }

    // ── CSRF protection ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /login without CSRF token is rejected")
    void loginPost_withoutCsrf_isRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "user@test.com")
                        .param("password", "pass"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /logout without CSRF token is rejected")
    @WithMockUser(authorities = "student")
    void logoutPost_withoutCsrf_isRejected() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().isForbidden());
    }

    // ── Disabled user login ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /register with CSRF token is accepted (public endpoint)")
    void registerPost_withCsrf_isAccepted() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Test")
                        .param("email", "test@student.xjtlu.edu.cn")
                        .param("password", "pass123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection());
    }
}

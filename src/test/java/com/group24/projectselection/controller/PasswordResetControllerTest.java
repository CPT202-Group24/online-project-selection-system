package com.group24.projectselection.controller;

import com.group24.projectselection.service.PasswordResetService;
import com.group24.projectselection.service.UserRegistrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private UserRegistrationService registrationService;

    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "s@student.xjtlu.edu.cn";
    }

    @AfterEach
    void tearDown() {
        testEmail = null;
    }

    @Test
    @DisplayName("POST /forgot-password calls service and redirects with info message")
    void processForgotPassword_callsServiceAndRedirects() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .param("email", testEmail))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password"))
                .andExpect(flash().attributeExists("infoMessage"));

        verify(passwordResetService).createResetTokenAndSendEmail(testEmail);
    }

    @Test
    @DisplayName("POST /reset-password with mismatched passwords returns error message")
    void processResetPassword_passwordsDoNotMatch_returnsError() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .param("token", "abc")
                        .param("password", "newPass1")
                        .param("confirmPassword", "newPass2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attribute("errorMessage", "Passwords do not match."));
    }

    @Test
    @DisplayName("POST /reset-password with valid token and matching passwords redirects to login")
    void processResetPassword_validToken_redirectsToLogin() throws Exception {
        when(registrationService.isValidPassword("NewPassword1")).thenReturn(true);

        mockMvc.perform(post("/reset-password")
                        .param("token", "valid-token")
                        .param("password", "NewPassword1")
                        .param("confirmPassword", "NewPassword1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?reset=true"));

        verify(passwordResetService).validateTokenAndResetPassword("valid-token", "NewPassword1");
    }

    // ── Security-focused tests ──────────────────────────────────────────

    @Test
    @DisplayName("POST /reset-password with short password returns error message")
    void processResetPassword_shortPassword_returnsError() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .param("token", "valid-token")
                        .param("password", "ShortPw")
                        .param("confirmPassword", "ShortPw"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attribute("errorMessage", "Password must be at least 8 characters and include uppercase, lowercase, and a digit."));

        verify(passwordResetService, never()).validateTokenAndResetPassword(any(), any());
    }

    @Test
    @DisplayName("POST /reset-password with expired token displays service error")
    void processResetPassword_expiredToken_displaysServiceError() throws Exception {
        when(registrationService.isValidPassword("newPassword1")).thenReturn(true);
        when(passwordResetService.validateTokenAndResetPassword("expired-token", "newPassword1"))
                .thenReturn("This reset link has expired.");

        mockMvc.perform(post("/reset-password")
                        .param("token", "expired-token")
                        .param("password", "newPassword1")
                        .param("confirmPassword", "newPassword1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attribute("errorMessage", "This reset link has expired."))
                .andExpect(model().attribute("token", "expired-token"));
    }

    @Test
    @DisplayName("POST /reset-password with used token displays service error")
    void processResetPassword_usedToken_displaysServiceError() throws Exception {
        when(registrationService.isValidPassword("newPassword1")).thenReturn(true);
        when(passwordResetService.validateTokenAndResetPassword("used-token", "newPassword1"))
                .thenReturn("This reset link has already been used.");

        mockMvc.perform(post("/reset-password")
                        .param("token", "used-token")
                        .param("password", "newPassword1")
                        .param("confirmPassword", "newPassword1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attribute("errorMessage", "This reset link has already been used."))
                .andExpect(model().attribute("token", "used-token"));
    }

    @Test
    @DisplayName("POST /reset-password with invalid token displays service error")
    void processResetPassword_invalidToken_displaysServiceError() throws Exception {
        when(registrationService.isValidPassword("newPassword1")).thenReturn(true);
        when(passwordResetService.validateTokenAndResetPassword("nonexistent", "newPassword1"))
                .thenReturn("Invalid reset link.");

        mockMvc.perform(post("/reset-password")
                        .param("token", "nonexistent")
                        .param("password", "newPassword1")
                        .param("confirmPassword", "newPassword1"))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attribute("errorMessage", "Invalid reset link."))
                .andExpect(model().attribute("token", "nonexistent"));
    }

    @Test
    @DisplayName("POST /reset-password with blank password is rejected")
    void processResetPassword_blankPassword_returnsError() throws Exception {
        mockMvc.perform(post("/reset-password")
                        .param("token", "valid-token")
                        .param("password", "")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"));

        verify(passwordResetService, never()).validateTokenAndResetPassword(any(), any());
    }

    @Test
    @DisplayName("POST /forgot-password always returns info message regardless of email existence")
    void processForgotPassword_alwaysReturnsInfoMessage() throws Exception {
        mockMvc.perform(post("/forgot-password")
                        .param("email", "nonexistent@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/forgot-password"))
                .andExpect(flash().attribute("infoMessage",
                        "If an account exists with that email, a reset link has been sent."));

        verify(passwordResetService).createResetTokenAndSendEmail("nonexistent@example.com");
    }
}

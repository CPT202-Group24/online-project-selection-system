package com.group24.projectselection.controller;

import com.group24.projectselection.service.PasswordResetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
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
        mockMvc.perform(post("/reset-password")
                        .param("token", "valid-token")
                        .param("password", "newPassword")
                        .param("confirmPassword", "newPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?reset=true"));

        verify(passwordResetService).validateTokenAndResetPassword("valid-token", "newPassword");
    }
}

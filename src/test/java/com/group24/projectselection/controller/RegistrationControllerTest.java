package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.service.UserRegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRegistrationService registrationService;

    @Test
    @DisplayName("blank name shows specific error message")
    void blankName_showsSpecificError() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "")
                        .param("email", "s@student.xjtlu.edu.cn")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", "Please enter your name."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("blank password shows specific error message")
    void blankPassword_showsSpecificError() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "Alice")
                        .param("email", "s@student.xjtlu.edu.cn")
                        .param("password", "")
                        .param("confirmPassword", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", "Please enter a password."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("mismatched passwords shows specific error message")
    void mismatchedPasswords_showsSpecificError() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "Alice")
                        .param("email", "s@student.xjtlu.edu.cn")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Different123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", "Passwords do not match."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("weak password shows specific error message")
    void weakPassword_showsSpecificError() throws Exception {
        when(registrationService.isValidPassword("123")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "Alice")
                        .param("email", "a@student.xjtlu.edu.cn")
                        .param("password", "123")
                        .param("confirmPassword", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage",
                        "Password must be at least 8 characters and include uppercase, lowercase, and a digit."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("non-university email shows specific error message")
    void nonUniversityEmail_showsSpecificError() throws Exception {
        when(registrationService.isValidPassword("Secret123")).thenReturn(true);
        when(registrationService.resolveRoleFromEmail("hacker@gmail.com")).thenReturn(null);

        mockMvc.perform(post("/register")
                        .param("name", "Hacker")
                        .param("email", "hacker@gmail.com")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage",
                        "Please use your university email (@student.xjtlu.edu.cn or @xjtlu.edu.cn)."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("duplicate email sets flash error and does not persist")
    void duplicateEmail_setsFlashErrorAndDoesNotPersist() throws Exception {
        when(registrationService.isValidPassword("Secret123")).thenReturn(true);
        when(registrationService.resolveRoleFromEmail("dup@student.xjtlu.edu.cn")).thenReturn(User.Role.student);
        when(registrationService.emailExists("dup@student.xjtlu.edu.cn")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("name", "Dup User")
                        .param("email", "dup@student.xjtlu.edu.cn")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", "An account with this email already exists."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("valid registration persists and redirects to login")
    void validRegistration_persistsAndRedirectsToLogin() throws Exception {
        when(registrationService.isValidPassword("Secret123")).thenReturn(true);
        when(registrationService.resolveRoleFromEmail("n@student.xjtlu.edu.cn")).thenReturn(User.Role.student);
        when(registrationService.emailExists("n@student.xjtlu.edu.cn")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "N")
                        .param("email", "n@student.xjtlu.edu.cn")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(registrationService).register(eq("N"), eq("n@student.xjtlu.edu.cn"), eq("Secret123"), eq(User.Role.student));
    }

    @Test
    @DisplayName("email is normalized before passing to service")
    void emailIsNormalizedBeforeServiceCalls() throws Exception {
        when(registrationService.isValidPassword("Secret123")).thenReturn(true);
        when(registrationService.resolveRoleFromEmail("n@student.xjtlu.edu.cn")).thenReturn(User.Role.student);
        when(registrationService.emailExists("n@student.xjtlu.edu.cn")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "N")
                        .param("email", "  N@STUDENT.XJTLU.EDU.CN  ")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(registrationService).emailExists("n@student.xjtlu.edu.cn");
        verify(registrationService).register(eq("N"), eq("n@student.xjtlu.edu.cn"), eq("Secret123"), eq(User.Role.student));
    }

    // ── Security-focused tests ──────────────────────────────────────────

    @Test
    @DisplayName("SQL injection in email parameter does not bypass validation")
    void sqlInjectionInEmail_doesNotBypassValidation() throws Exception {
        when(registrationService.isValidPassword("Secret123")).thenReturn(true);
        when(registrationService.resolveRoleFromEmail("'; drop table users; --@student.xjtlu.edu.cn")).thenReturn(null);

        mockMvc.perform(post("/register")
                        .param("name", "Hacker")
                        .param("email", "'; DROP TABLE users; --@student.xjtlu.edu.cn")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage",
                        "Please use your university email (@student.xjtlu.edu.cn or @xjtlu.edu.cn)."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("XSS payload in name does not bypass validation (name is not blank)")
    void xssInName_doesNotBypassValidation() throws Exception {
        when(registrationService.isValidPassword("Secret123")).thenReturn(true);
        when(registrationService.resolveRoleFromEmail("s@student.xjtlu.edu.cn")).thenReturn(User.Role.student);
        when(registrationService.emailExists("s@student.xjtlu.edu.cn")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "<script>alert('xss')</script>")
                        .param("email", "s@student.xjtlu.edu.cn")
                        .param("password", "Secret123")
                        .param("confirmPassword", "Secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(registrationService).register(eq("<script>alert('xss')</script>"), eq("s@student.xjtlu.edu.cn"), eq("Secret123"), eq(User.Role.student));
    }
}

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

    private static final String VALIDATION_ERROR =
            "Invalid email or missing required information.";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRegistrationService registrationService;

    @Test
    @DisplayName("invalid registration sets flash error and does not persist")
    void invalidRegistration_setsFlashErrorAndDoesNotPersist() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "N")
                        .param("email", "bad@xjtlu.edu.cn")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("valid registration persists and redirects to login")
    void validRegistration_persistsAndRedirectsToLogin() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(true);
        when(registrationService.emailExists(any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "N")
                        .param("email", "n@student.xjtlu.edu.cn")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(registrationService).register(eq("N"), eq("n@student.xjtlu.edu.cn"), eq("secret123"), eq(User.Role.student));
    }

    @Test
    @DisplayName("duplicate email sets flash error and does not persist")
    void duplicateEmail_setsFlashErrorAndDoesNotPersist() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(true);
        when(registrationService.emailExists("dup@student.xjtlu.edu.cn")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("name", "Dup User")
                        .param("email", "dup@student.xjtlu.edu.cn")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", "An account with this email already exists."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("weak password is rejected by service validation")
    void weakPassword_setsFlashErrorAndDoesNotPersist() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "Alice")
                        .param("email", "a@student.xjtlu.edu.cn")
                        .param("password", "123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("email is normalized before passing to service")
    void emailIsNormalizedBeforeServiceCalls() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(true);
        when(registrationService.emailExists("n@student.xjtlu.edu.cn")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "N")
                        .param("email", "  N@STUDENT.XJTLU.EDU.CN  ")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(registrationService).isValidRegistrationInput(eq("N"), eq("n@student.xjtlu.edu.cn"), eq("secret123"), eq(User.Role.student));
        verify(registrationService).emailExists("n@student.xjtlu.edu.cn");
        verify(registrationService).register(eq("N"), eq("n@student.xjtlu.edu.cn"), eq("secret123"), eq(User.Role.student));
    }

    // ── Security-focused tests ──────────────────────────────────────────

    @Test
    @DisplayName("admin role registration is rejected - parseRegisterableRole returns null")
    void adminRoleRegistration_setsFlashError() throws Exception {
        when(registrationService.parseRegisterableRole("admin")).thenReturn(null);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "Admin")
                        .param("email", "admin@xjtlu.edu.cn")
                        .param("password", "secret123")
                        .param("role", "admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("blank name is rejected with flash error")
    void blankName_setsFlashError() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(eq(""), any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "")
                        .param("email", "s@student.xjtlu.edu.cn")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("blank email is rejected with flash error")
    void blankEmail_setsFlashError() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), eq(""), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "Alice")
                        .param("email", "")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("blank password is rejected with flash error")
    void blankPassword_setsFlashError() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), eq(""), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "Alice")
                        .param("email", "s@student.xjtlu.edu.cn")
                        .param("password", "")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("SQL injection in email parameter does not bypass validation")
    void sqlInjectionInEmail_doesNotBypassValidation() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "Hacker")
                        .param("email", "'; DROP TABLE users; --@student.xjtlu.edu.cn")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    @DisplayName("XSS payload in name parameter does not bypass validation")
    void xssInName_doesNotBypassValidation() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "<script>alert('xss')</script>")
                        .param("email", "s@student.xjtlu.edu.cn")
                        .param("password", "secret123")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }
}

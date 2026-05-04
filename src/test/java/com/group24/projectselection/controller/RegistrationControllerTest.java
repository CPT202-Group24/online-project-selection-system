package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.service.UserRegistrationService;
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
    void invalidRegistration_setsFlashErrorAndDoesNotPersist() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "N")
                        .param("email", "bad@xjtlu.edu.cn")
                        .param("password", "p")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", VALIDATION_ERROR));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }

    @Test
    void validRegistration_persistsAndRedirectsToLogin() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(true);
        when(registrationService.emailExists(any())).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("name", "N")
                        .param("email", "n@student.xjtlu.edu.cn")
                        .param("password", "secret")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        verify(registrationService).register(eq("N"), eq("n@student.xjtlu.edu.cn"), eq("secret"), eq(User.Role.student));
    }

    @Test
    void duplicateEmail_setsFlashErrorAndDoesNotPersist() throws Exception {
        when(registrationService.parseRegisterableRole("student")).thenReturn(User.Role.student);
        when(registrationService.isValidRegistrationInput(any(), any(), any(), any())).thenReturn(true);
        when(registrationService.emailExists("dup@student.xjtlu.edu.cn")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("name", "Dup User")
                        .param("email", "dup@student.xjtlu.edu.cn")
                        .param("password", "secret")
                        .param("role", "student"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("errorMessage", "An account with this email already exists."));

        verify(registrationService, never()).register(any(), any(), any(), any());
    }
}

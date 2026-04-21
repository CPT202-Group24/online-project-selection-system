package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("null")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    private User existingUser;
    private Authentication auth;

    @BeforeAll
    static void beforeAll() {
        // reserved for one-time setup when shared fixtures are needed
    }

    @AfterAll
    static void afterAll() {
        // reserved for one-time cleanup when shared fixtures are needed
    }

    @BeforeEach
    void setUp() {
        existingUser = buildExistingUser();
        auth = new UsernamePasswordAuthenticationToken("student1@student.xjtlu.edu.cn", "n/a");
    }

    @Test
    @DisplayName("Save profile with required fields persists and redirects to success")
    void saveProfile_withMandatoryFields_updatesDatabaseAndRedirectsWithSuccess() throws Exception {
        when(userRepository.findByEmail("student1@student.xjtlu.edu.cn")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(isA(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

        mockMvc.perform(post("/profile")
                        .principal(auth)
                        .param("name", "Student One Updated")
                        .param("phone", "18812345678")
                        .param("department", "Computer Science"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?success=true"));

        verify(userRepository).save(isA(User.class));
    }

    @Test
    @DisplayName("Empty phone blocks save and returns field required validation error")
    void saveProfile_withEmptyMandatoryField_blocksSaveAndShowsRequiredWarning() throws Exception {
        when(userRepository.findByEmail("student1@student.xjtlu.edu.cn")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/profile")
                        .principal(auth)
                        .param("name", "Student One Updated")
                        .param("phone", "")
                        .param("department", "Computer Science"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("profileForm", "phone"))
                .andExpect(model().attributeHasFieldErrorCode("profileForm", "phone", "NotBlank"));

        verify(userRepository, never()).save(isA(User.class));
    }

    @Test
    @DisplayName("Empty department blocks save and returns field required validation error")
    void saveProfile_withEmptyDepartment_blocksSaveAndShowsRequiredWarning() throws Exception {
        when(userRepository.findByEmail("student1@student.xjtlu.edu.cn")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/profile")
                        .principal(auth)
                        .param("name", "Student One Updated")
                        .param("phone", "18812345678")
                        .param("department", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("profileForm", "department"))
                .andExpect(model().attributeHasFieldErrorCode("profileForm", "department", "NotBlank"));

        verify(userRepository, never()).save(isA(User.class));
    }

    private User buildExistingUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("student1@student.xjtlu.edu.cn");
        user.setName("Student One");
        user.setPhone("13000000000");
        user.setDepartment("Software Engineering");
        user.setRole(User.Role.student);
        user.setStatus(User.UserStatus.active);
        return user;
    }
}

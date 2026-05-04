package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationApiController.class)
@AutoConfigureMockMvc
class ApplicationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "student@student.xjtlu.edu.cn", roles = "student")
    void testSubmit_ShortStatement_Returns400() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .param("projectId", "1")
                        .param("personalStatement", "too short")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "student@student.xjtlu.edu.cn", roles = "student")
    void testSubmit_Duplicate_Returns409() throws Exception {
        User student = new User();
        student.setId(1L);
        student.setEmail("student@student.xjtlu.edu.cn");

        when(userRepository.findByEmail("student@student.xjtlu.edu.cn"))
                .thenReturn(Optional.of(student));
        when(applicationService.submitApplication(any(User.class), eq(1L), any(String.class)))
                .thenThrow(new IllegalStateException("You have already applied to this project."));

        String longStatement = "I am very interested in this project because I have relevant experience in this field and I want to learn more.";

        mockMvc.perform(post("/api/applications")
                        .param("projectId", "1")
                        .param("personalStatement", longStatement)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}

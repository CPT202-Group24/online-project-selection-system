package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.service.TeacherApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherApprovalController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherApprovalService teacherApprovalService;

    @Test
    void testViewAcceptedStudents_Success() throws Exception {
        Application mockApp = new Application();
        mockApp.setId(1L);
        mockApp.setStatus(Application.ApplicationStatus.accepted);

        when(teacherApprovalService.getAcceptedApplicationsByTeacherEmail(100L, "teacher@test.com"))
                .thenReturn(List.of(mockApp));

        mockMvc.perform(get("/api/teacher/applications/topics/100/students")
                        .principal(new UsernamePasswordAuthenticationToken(
                                "teacher@test.com",
                                null,
                                Collections.emptyList()
                        )))
                .andExpect(status().isOk());
    }

    @Test
    void testViewAcceptedStudents_Forbidden() throws Exception {
        when(teacherApprovalService.getAcceptedApplicationsByTeacherEmail(100L, "other@test.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/api/teacher/applications/topics/100/students")
                        .principal(new UsernamePasswordAuthenticationToken(
                                "other@test.com",
                                null,
                                Collections.emptyList()
                        )))
                .andExpect(status().isForbidden());
    }
}

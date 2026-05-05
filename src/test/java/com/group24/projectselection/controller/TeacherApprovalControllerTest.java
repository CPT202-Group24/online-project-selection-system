package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.service.TeacherApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

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

        when(teacherApprovalService.getAcceptedApplications(100L, 10L))
                .thenReturn(List.of(mockApp));

        mockMvc.perform(get("/api/teacher/applications/topics/100/students")
                        .param("teacherId", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void testViewAcceptedStudents_Forbidden() throws Exception {
        when(teacherApprovalService.getAcceptedApplications(100L, 99L))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/api/teacher/applications/topics/100/students")
                        .param("teacherId", "99"))
                .andExpect(status().isForbidden());
    }
}

package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.ConflictLog;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ConflictLogRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherApprovalServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @Mock
    private ConflictLogRepository conflictLogRepository;

    @InjectMocks
    private TeacherApprovalServiceImpl teacherApprovalService;

    @Test
    void testProcessApproval_Accepted_NotFull_KeepsOtherPending() {
        User student = new User();
        student.setId(10L);

        User otherStudent = new User();
        otherStudent.setId(11L);

        ProjectTopic mockProject = new ProjectTopic();
        mockProject.setId(100L);
        mockProject.setStatus(ProjectTopic.TopicStatus.requested);
        mockProject.setMaxStudents(2);

        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setStudent(student);
        mainApp.setProject(mockProject);
        mainApp.setStatus(Application.ApplicationStatus.pending);

        Application otherApp = new Application();
        otherApp.setId(2L);
        otherApp.setStudent(otherStudent);
        otherApp.setProject(mockProject);
        otherApp.setStatus(Application.ApplicationStatus.pending);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));
        when(applicationRepository.findByStudentId(10L)).thenReturn(Arrays.asList(mainApp));
        when(applicationRepository.findByProjectId(100L)).thenReturn(Arrays.asList(mainApp, otherApp));

        teacherApprovalService.processApproval(1L, true);

        assertEquals(Application.ApplicationStatus.accepted, mainApp.getStatus());
        assertEquals(ProjectTopic.TopicStatus.requested, mockProject.getStatus());
        assertEquals(Application.ApplicationStatus.pending, otherApp.getStatus());

        verify(conflictLogRepository, never()).save(any(ConflictLog.class));
    }

    @Test
    void testProcessApproval_Accepted_ProjectFull_AutoRejectsOtherPending() {
        User student = new User();
        student.setId(10L);

        User otherStudent = new User();
        otherStudent.setId(11L);

        ProjectTopic mockProject = new ProjectTopic();
        mockProject.setId(100L);
        mockProject.setStatus(ProjectTopic.TopicStatus.requested);
        mockProject.setMaxStudents(1);

        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setStudent(student);
        mainApp.setProject(mockProject);
        mainApp.setStatus(Application.ApplicationStatus.pending);

        Application otherApp = new Application();
        otherApp.setId(2L);
        otherApp.setStudent(otherStudent);
        otherApp.setProject(mockProject);
        otherApp.setStatus(Application.ApplicationStatus.pending);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));
        when(applicationRepository.findByStudentId(10L)).thenReturn(Arrays.asList(mainApp));
        when(applicationRepository.findByProjectId(100L)).thenReturn(Arrays.asList(mainApp, otherApp));

        teacherApprovalService.processApproval(1L, true);

        assertEquals(Application.ApplicationStatus.accepted, mainApp.getStatus());
        assertEquals(ProjectTopic.TopicStatus.agreed, mockProject.getStatus());
        assertEquals(Application.ApplicationStatus.rejected, otherApp.getStatus());

        verify(conflictLogRepository, times(1)).save(any(ConflictLog.class));
    }

    @Test
    void testProcessApproval_Accepted_AutoRejectsOtherPendingApplicationsFromSameStudent() {
        User student = new User();
        student.setId(10L);

        ProjectTopic acceptedProject = new ProjectTopic();
        acceptedProject.setId(100L);
        acceptedProject.setStatus(ProjectTopic.TopicStatus.requested);
        acceptedProject.setMaxStudents(2);

        ProjectTopic otherProject = new ProjectTopic();
        otherProject.setId(200L);
        otherProject.setStatus(ProjectTopic.TopicStatus.available);
        otherProject.setMaxStudents(2);

        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setStudent(student);
        mainApp.setProject(acceptedProject);
        mainApp.setStatus(Application.ApplicationStatus.pending);

        Application otherPendingApp = new Application();
        otherPendingApp.setId(2L);
        otherPendingApp.setStudent(student);
        otherPendingApp.setProject(otherProject);
        otherPendingApp.setStatus(Application.ApplicationStatus.pending);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));
        when(applicationRepository.findByStudentId(10L)).thenReturn(Arrays.asList(mainApp, otherPendingApp));
        when(applicationRepository.findByProjectId(100L)).thenReturn(Arrays.asList(mainApp));

        teacherApprovalService.processApproval(1L, true);

        assertEquals(Application.ApplicationStatus.accepted, mainApp.getStatus());
        assertEquals(Application.ApplicationStatus.rejected, otherPendingApp.getStatus());
        assertEquals(ProjectTopic.TopicStatus.requested, acceptedProject.getStatus());

        verify(conflictLogRepository, times(1)).save(any(ConflictLog.class));
    }

    @Test
    void testProcessApproval_Rejected_Success() {
        ProjectTopic mockProject = new ProjectTopic();
        mockProject.setId(100L);
        mockProject.setStatus(ProjectTopic.TopicStatus.requested);

        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setProject(mockProject);
        mainApp.setStatus(Application.ApplicationStatus.pending);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));

        teacherApprovalService.processApproval(1L, false);

        assertEquals(Application.ApplicationStatus.rejected, mainApp.getStatus());
        assertEquals(ProjectTopic.TopicStatus.requested, mockProject.getStatus());
    }

    @Test
    void testProcessApproval_ApplicationNotFound_ThrowsException() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teacherApprovalService.processApproval(99L, true);
        });

        assertEquals("Application not found", exception.getMessage());
    }

    @Test
    void testProcessApproval_NotPending_ThrowsException() {
        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setStatus(Application.ApplicationStatus.accepted);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teacherApprovalService.processApproval(1L, true);
        });

        assertEquals("Only pending applications can be processed", exception.getMessage());
    }

    @Test
    void testProcessApproval_StudentAlreadyAccepted_ThrowsExceptionAndLogsConflict() {
        User student = new User();
        student.setId(10L);

        ProjectTopic newProject = new ProjectTopic();
        newProject.setId(100L);
        newProject.setStatus(ProjectTopic.TopicStatus.requested);
        newProject.setMaxStudents(2);

        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setStudent(student);
        mainApp.setProject(newProject);
        mainApp.setStatus(Application.ApplicationStatus.pending);

        Application acceptedApp = new Application();
        acceptedApp.setId(2L);
        acceptedApp.setStudent(student);
        acceptedApp.setStatus(Application.ApplicationStatus.accepted);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));
        when(applicationRepository.findByStudentId(10L)).thenReturn(Arrays.asList(mainApp, acceptedApp));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            teacherApprovalService.processApproval(1L, true);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Student already has an accepted application", exception.getReason());
        verify(conflictLogRepository, times(1)).save(any(ConflictLog.class));
    }

    @Test
    void testProcessApproval_ProjectCapacityReached_ThrowsExceptionAndLogsConflict() {
        User student = new User();
        student.setId(10L);

        ProjectTopic project = new ProjectTopic();
        project.setId(100L);
        project.setStatus(ProjectTopic.TopicStatus.requested);
        project.setMaxStudents(1);

        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setStudent(student);
        mainApp.setProject(project);
        mainApp.setStatus(Application.ApplicationStatus.pending);

        Application acceptedApp = new Application();
        acceptedApp.setId(2L);
        acceptedApp.setProject(project);
        acceptedApp.setStatus(Application.ApplicationStatus.accepted);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));
        when(applicationRepository.findByStudentId(10L)).thenReturn(Arrays.asList(mainApp));
        when(applicationRepository.findByProjectId(100L)).thenReturn(Arrays.asList(mainApp, acceptedApp));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            teacherApprovalService.processApproval(1L, true);
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Project has reached maximum student capacity", exception.getReason());
        verify(conflictLogRepository, times(1)).save(any(ConflictLog.class));
    }
}
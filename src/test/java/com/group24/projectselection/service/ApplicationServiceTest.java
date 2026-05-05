package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.Application.ApplicationStatus;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void testSubmitApplication_Success_StatusIsPending() {
        User student = new User();
        student.setId(1L);

        ProjectTopic project = new ProjectTopic();
        project.setId(10L);
        project.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findById(10L)).thenReturn(Optional.of(project));
        when(applicationRepository.findByStudentId(1L)).thenReturn(new ArrayList<>());
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(call -> call.getArgument(0));

        Application result = applicationService.submitApplication(student, 10L, "I am interested");

        assertEquals(ApplicationStatus.pending, result.getStatus());
    }

    @Test
    void testSubmitApplication_Duplicate_ThrowsError() {
        User student = new User();
        student.setId(1L);

        ProjectTopic project = new ProjectTopic();
        project.setId(10L);
        project.setStatus(ProjectTopic.TopicStatus.available);

        Application existingApp = new Application();
        existingApp.setProject(project);
        existingApp.setStatus(ApplicationStatus.pending);

        List<Application> existingList = new ArrayList<>();
        existingList.add(existingApp);

        when(projectTopicRepository.findById(10L)).thenReturn(Optional.of(project));
        when(applicationRepository.findByStudentId(1L)).thenReturn(existingList);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            applicationService.submitApplication(student, 10L, "I am interested");
        });

        assertEquals("You have already applied to this project.", exception.getMessage());
    }

    @Test
    void testWithdrawApplication_Success() {
        User student = new User();
        student.setId(1L);

        Application app = new Application();
        app.setId(5L);
        app.setStudent(student);
        app.setStatus(ApplicationStatus.pending);

        when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

        applicationService.withdrawApplication(student, 5L);

        assertEquals(ApplicationStatus.withdrawn, app.getStatus());
    }

    @Test
    void testWithdrawApplication_NotPending_ThrowsError() {
        User student = new User();
        student.setId(1L);

        Application app = new Application();
        app.setId(5L);
        app.setStudent(student);
        app.setStatus(ApplicationStatus.accepted);

        when(applicationRepository.findById(5L)).thenReturn(Optional.of(app));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            applicationService.withdrawApplication(student, 5L);
        });

        assertEquals("Only pending applications can be withdrawn.", exception.getMessage());
    }
}

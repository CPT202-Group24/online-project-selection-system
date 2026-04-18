package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private TeacherApprovalServiceImpl teacherApprovalService;

    // 对应 UT-M6-S2-01: 成功同意申请
    @Test
    void testProcessApproval_Accepted_Success() {
        ProjectTopic mockProject = new ProjectTopic();
        mockProject.setId(100L);
        mockProject.setStatus(ProjectTopic.TopicStatus.requested);

        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setProject(mockProject);
        mainApp.setStatus(Application.ApplicationStatus.pending);

        Application otherApp = new Application();
        otherApp.setId(2L);
        otherApp.setProject(mockProject);
        otherApp.setStatus(Application.ApplicationStatus.pending);

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));
        when(applicationRepository.findByProjectId(100L)).thenReturn(Arrays.asList(mainApp, otherApp));

        teacherApprovalService.processApproval(1L, true);

        assertEquals(Application.ApplicationStatus.accepted, mainApp.getStatus());
        assertEquals(ProjectTopic.TopicStatus.agreed, mockProject.getStatus());
        assertEquals(Application.ApplicationStatus.rejected, otherApp.getStatus());
    }

    // 对应 UT-M6-S2-02: 成功拒绝申请
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
        assertEquals(ProjectTopic.TopicStatus.requested, mockProject.getStatus()); // 课题状态保持不变
    }

    // 对应 UT-M6-S3-01: 报错测试 - 找不到申请单
    @Test
    void testProcessApproval_ApplicationNotFound_ThrowsException() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teacherApprovalService.processApproval(99L, true);
        });

        assertEquals("Application not found", exception.getMessage());
    }

    // 对应 UT-M6-S3-02: 报错测试 - 申请单状态不是 pending
    @Test
    void testProcessApproval_NotPending_ThrowsException() {
        Application mainApp = new Application();
        mainApp.setId(1L);
        mainApp.setStatus(Application.ApplicationStatus.accepted); // 故意设置为已处理状态

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(mainApp));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            teacherApprovalService.processApproval(1L, true);
        });

        assertEquals("Only pending applications can be processed", exception.getMessage());
    }
}

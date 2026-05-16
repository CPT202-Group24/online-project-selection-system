package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ConflictLogRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProjectServiceTest {

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ConflictLogRepository conflictLogRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminProjectService adminProjectService;

    @BeforeEach
    void initDefaultApplicationList() {
        lenient().when(applicationRepository.findByProjectId(any())).thenReturn(List.of());
    }

    // UT-M8-S3-01
    @Test
    void forceArchive_shouldArchiveTopicAndKeepPreviousStatus() {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(1L);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(projectTopicRepository.save(any(ProjectTopic.class))).thenAnswer(i -> i.getArgument(0));

        ProjectTopic result = adminProjectService.forceArchive(1L);

        assertEquals(ProjectTopic.TopicStatus.archived, result.getStatus());
        assertEquals(ProjectTopic.TopicStatus.available, result.getPreviousStatus());
    }

    // UT-M8-S3-02
    @Test
    void forceArchive_shouldThrowWhenTopicAlreadyArchived() {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(2L);
        topic.setStatus(ProjectTopic.TopicStatus.archived);

        when(projectTopicRepository.findById(2L)).thenReturn(Optional.of(topic));

        assertThrows(IllegalStateException.class, () -> adminProjectService.forceArchive(2L));
    }

    // UT-M8-S3-03
    @Test
    void restore_shouldRestorePreviousStatus() {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(3L);
        topic.setStatus(ProjectTopic.TopicStatus.archived);
        topic.setPreviousStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findById(3L)).thenReturn(Optional.of(topic));
        when(projectTopicRepository.save(any(ProjectTopic.class))).thenAnswer(i -> i.getArgument(0));

        ProjectTopic result = adminProjectService.restore(3L);

        assertEquals(ProjectTopic.TopicStatus.available, result.getStatus());
        assertNull(result.getPreviousStatus());
    }

    // UT-M8-S3-04
    @Test
    void restore_shouldThrowWhenTopicIsNotArchived() {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(4L);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findById(4L)).thenReturn(Optional.of(topic));

        assertThrows(IllegalStateException.class, () -> adminProjectService.restore(4L));
    }

    // UT-M8-S3-05
    @Test
    void forceArchive_shouldThrowWhenTopicDoesNotExist() {
        when(projectTopicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> adminProjectService.forceArchive(99L));
    }

    // UT-M8-S3-06
    @Test
    void deletePermanently_shouldClearRelatedRecordsBeforeDeletingTopic() {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(5L);

        when(projectTopicRepository.findById(5L)).thenReturn(Optional.of(topic));

        adminProjectService.deletePermanently(5L);

        verify(conflictLogRepository).deleteByProjectId(5L);
        verify(applicationRepository).deleteByProjectId(5L);
        verify(projectTopicRepository).delete(topic);
    }
}

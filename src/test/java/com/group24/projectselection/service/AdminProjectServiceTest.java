package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Sprint 3 — M8 PBI 8.5（强制归档 / 恢复）Service 层单元测试（方式 A：Mockito mock 仓储）。
 */
@ExtendWith(MockitoExtension.class)
class AdminProjectServiceTest {

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @InjectMocks
    private AdminProjectService adminProjectService;

    // UT-M8-S3-01
    @Test
    void forceArchive_正常情况_归档后状态应为archived且保存先前状态() {
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
    void forceArchive_异常情况_对已归档项目再次归档应抛出IllegalStateException() {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(2L);
        topic.setStatus(ProjectTopic.TopicStatus.archived);

        when(projectTopicRepository.findById(2L)).thenReturn(Optional.of(topic));

        assertThrows(IllegalStateException.class, () -> adminProjectService.forceArchive(2L));
    }

    // UT-M8-S3-03
    @Test
    void restore_正常情况_归档项目恢复后应回到先前状态() {
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
    void restore_异常情况_对非归档项目恢复应抛出IllegalStateException() {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(4L);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findById(4L)).thenReturn(Optional.of(topic));

        assertThrows(IllegalStateException.class, () -> adminProjectService.restore(4L));
    }

    // UT-M8-S3-05
    @Test
    void forceArchive_异常情况_项目不存在应抛出NoSuchElementException() {
        when(projectTopicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> adminProjectService.forceArchive(99L));
    }
}

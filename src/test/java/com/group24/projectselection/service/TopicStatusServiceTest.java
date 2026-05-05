package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopicStatusServiceTest {

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @InjectMocks
    private TopicStatusServiceImpl topicStatusService;

    @Test
    void publishTopic_unpublishedTopic_statusChangesToAvailable() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.unpublished);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));
        when(projectTopicRepository.save(any(ProjectTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTopic result = topicStatusService.publishTopic(10L, 1L);

        assertEquals(ProjectTopic.TopicStatus.available, result.getStatus());
    }

    @Test
    void publishTopic_alreadyAvailable_throwsIllegalStateException() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> topicStatusService.publishTopic(10L, 1L)
        );

        assertEquals("Only unpublished topics can be published", exception.getMessage());
    }

    @Test
    void publishTopic_topicBelongsToDifferentTeacher_throwsIllegalArgumentException() {
        when(projectTopicRepository.findByIdAndTeacherId(10L, 99L))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> topicStatusService.publishTopic(10L, 99L)
        );

        assertEquals("Topic not found or access denied", exception.getMessage());
    }

    @Test
    void closeTopic_availableTopic_statusChangesToClosed() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));
        when(projectTopicRepository.save(any(ProjectTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTopic result = topicStatusService.closeTopic(10L, 1L);

        assertEquals(ProjectTopic.TopicStatus.closed, result.getStatus());
    }

    @Test
    void closeTopic_alreadyClosed_throwsIllegalStateException() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.closed);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> topicStatusService.closeTopic(10L, 1L)
        );

        assertEquals("Topic is already closed", exception.getMessage());
    }

    @Test
    void closeTopic_unpublishedTopic_throwsIllegalStateException() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.unpublished);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> topicStatusService.closeTopic(10L, 1L)
        );

        assertEquals("Only published topics can be closed", exception.getMessage());
    }

    @Test
    void archiveTopic_closedTopic_statusChangesToArchived() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.closed);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));
        when(projectTopicRepository.save(any(ProjectTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTopic result = topicStatusService.archiveTopic(10L, 1L);

        assertEquals(ProjectTopic.TopicStatus.archived, result.getStatus());
    }

    @Test
    void archiveTopic_alreadyArchived_throwsIllegalStateException() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.archived);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> topicStatusService.archiveTopic(10L, 1L)
        );

        assertEquals("Topic is already archived", exception.getMessage());
    }

    @Test
    void archiveTopic_notClosed_throwsIllegalStateException() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(10L);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findByIdAndTeacherId(10L, 1L))
                .thenReturn(Optional.of(topic));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> topicStatusService.archiveTopic(10L, 1L)
        );

        assertEquals("Topic must be closed before it can be archived", exception.getMessage());
    }
}

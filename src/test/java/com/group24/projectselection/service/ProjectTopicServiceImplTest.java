package com.group24.projectselection.service;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectTopicServiceImplTest {

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProjectTopicServiceImpl projectTopicService;

    @Test
    void createProjectTopic_shouldNormalizeKeywordsBeforeSaving() {
        User teacher = new User();
        teacher.setId(1L);

        Category category = new Category();
        category.setId(10L);

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("AI Project");
        topic.setDescription("Test description");
        topic.setRequiredSkills("Java");
        topic.setKeywords("AI, Java, AI, Spring ");
        topic.setCategory(category);
        topic.setMaxStudents(2);

        when(projectTopicRepository.save(any(ProjectTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTopic saved = projectTopicService.createProjectTopic(topic, teacher);

        assertNotNull(saved);
        assertEquals("AI, Java, Spring", saved.getKeywords());
        assertEquals(ProjectTopic.TopicStatus.unpublished, saved.getStatus());
        assertFalse(saved.isDraft());
        assertEquals(teacher, saved.getTeacher());

        verify(projectTopicRepository, times(1)).save(any(ProjectTopic.class));
    }

    @Test
    void createDraftProject_shouldSaveAsUnpublishedDraftWhenCategoryIsEmpty() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("Draft Topic");
        topic.setDescription("Draft description");
        topic.setRequiredSkills("Spring Boot");
        topic.setKeywords("draft, test");
        topic.setMaxStudents(1);

        when(projectTopicRepository.save(any(ProjectTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTopic saved = projectTopicService.createDraftProject(topic, teacher);

        assertNotNull(saved);
        assertEquals(ProjectTopic.TopicStatus.unpublished, saved.getStatus());
        assertTrue(saved.isDraft());
        assertNull(saved.getCategory());
        assertEquals(teacher, saved.getTeacher());

        verify(projectTopicRepository, times(1)).save(any(ProjectTopic.class));
    }

    @Test
    void createProjectTopic_shouldThrowExceptionWhenCategoryIsMissing() {
        User teacher = new User();
        teacher.setId(1L);

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("No Category Topic");
        topic.setDescription("Test");
        topic.setRequiredSkills("Java");
        topic.setKeywords("AI, Java");
        topic.setMaxStudents(1);
        topic.setCategory(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectTopicService.createProjectTopic(topic, teacher)
        );

        assertEquals("Please select a category before saving.", exception.getMessage());
        verify(projectTopicRepository, never()).save(any(ProjectTopic.class));
    }

    @Test
    void deleteProjectTopic_whenUnpublishedAndOwned_deletesTopic() {
        Long topicId = 1L;
        Long teacherId = 10L;

        User teacher = new User();
        teacher.setId(teacherId);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(topicId);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.unpublished);

        when(projectTopicRepository.findByIdAndTeacherId(topicId, teacherId))
                .thenReturn(java.util.Optional.of(topic));

        projectTopicService.deleteProjectTopic(topicId, teacherId);

        verify(projectTopicRepository, times(1)).delete(topic);
    }

    @Test
    void deleteProjectTopic_whenTopicIsAvailable_throwsException() {
        Long topicId = 1L;
        Long teacherId = 10L;

        User teacher = new User();
        teacher.setId(teacherId);

        ProjectTopic topic = new ProjectTopic();
        topic.setId(topicId);
        topic.setTeacher(teacher);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findByIdAndTeacherId(topicId, teacherId))
                .thenReturn(java.util.Optional.of(topic));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectTopicService.deleteProjectTopic(topicId, teacherId)
        );

        assertEquals("Only unpublished project topics can be deleted", exception.getMessage());
        verify(projectTopicRepository, never()).delete(any(ProjectTopic.class));
    }

    @Test
    void deleteProjectTopic_whenTopicNotOwned_throwsException() {
        Long topicId = 1L;
        Long teacherId = 10L;

        when(projectTopicRepository.findByIdAndTeacherId(topicId, teacherId))
                .thenReturn(java.util.Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectTopicService.deleteProjectTopic(topicId, teacherId)
        );

        assertEquals("You cannot delete this project", exception.getMessage());
        verify(projectTopicRepository, never()).delete(any(ProjectTopic.class));
    }

}

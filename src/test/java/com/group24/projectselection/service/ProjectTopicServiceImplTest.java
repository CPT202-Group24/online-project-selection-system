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
}
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectTopicServiceTest {

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProjectTopicServiceImpl projectTopicService;

    @Test
    void createProjectTopic_setsCorrectTeacherAndUnpublishedStatus() {
        User teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@test.com");

        Category category = new Category();
        category.setId(1L);

        ProjectTopic projectTopic = new ProjectTopic();
        projectTopic.setTitle("AI Research");
        projectTopic.setDescription("Study AI methods");
        projectTopic.setRequiredSkills("Java, Spring");
        projectTopic.setKeywords("AI, Java");
        projectTopic.setMaxStudents(3);
        projectTopic.setCategory(category);

        when(projectTopicRepository.save(any(ProjectTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTopic result = projectTopicService.createProjectTopic(projectTopic, teacher);

        assertEquals(teacher, result.getTeacher());
        assertEquals(1L, result.getTeacher().getId());
        assertEquals(ProjectTopic.TopicStatus.unpublished, result.getStatus());
    }

    @Test
    void updateProjectTopic_calledByDifferentUser_shouldBeRejected() {
        Long differentTeacherId = 2L;

        Category category = new Category();
        category.setId(1L);

        ProjectTopic updatedProject = new ProjectTopic();
        updatedProject.setId(100L);
        updatedProject.setTitle("Unauthorized Edit");
        updatedProject.setCategory(category);

        when(projectTopicRepository.findByIdAndTeacherId(100L, differentTeacherId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectTopicService.updateProjectTopic(updatedProject, differentTeacherId)
        );

        assertEquals("You cannot modify this project", exception.getMessage());
    }

    @Test
    void searchAvailableTopics_withKeywordAndCategory_returnsMatchingTopics() {
        String keyword = "Java";
        Long categoryId = 1L;

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("Java Web Project");

        when(projectTopicRepository.searchTopicsByKeywordAndCategory(
                eq(ProjectTopic.TopicStatus.available),
                eq(keyword),
                eq(categoryId),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(topic)));

        Page<ProjectTopic> result = projectTopicService.searchAvailableTopics(
                keyword,
                categoryId,
                0,
                10,
                "newest"
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Java Web Project", result.getContent().get(0).getTitle());
    }

    @Test
    void searchAvailableTopics_noMatch_returnsEmptyPage() {
        String keyword = "Python";
        Long categoryId = 2L;

        when(projectTopicRepository.searchTopicsByKeywordAndCategory(
                eq(ProjectTopic.TopicStatus.available),
                eq(keyword),
                eq(categoryId),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        Page<ProjectTopic> result = projectTopicService.searchAvailableTopics(
                keyword,
                categoryId,
                0,
                10,
                "newest"
        );

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }
    @Test
    void searchAvailableTopics_withNewestSort_usesIdDescendingPageRequest() {
        String keyword = "Java";
        Long categoryId = 1L;

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("Java Web Project");

        when(projectTopicRepository.searchTopicsByKeywordAndCategory(
                eq(ProjectTopic.TopicStatus.available),
                eq(keyword),
                eq(categoryId),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(topic)));

        Page<ProjectTopic> result = projectTopicService.searchAvailableTopics(
                keyword,
                categoryId,
                1,
                10,
                "newest"
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(projectTopicRepository).searchTopicsByKeywordAndCategory(
                eq(ProjectTopic.TopicStatus.available),
                eq(keyword),
                eq(categoryId),
                argThat(pageable ->
                        pageable.getPageNumber() == 1 &&
                                pageable.getPageSize() == 10 &&
                                pageable.getSort().getOrderFor("id") != null &&
                                pageable.getSort().getOrderFor("id").isDescending()
                )
        );
    }
    @Test
    void searchAvailableTopics_withAzSort_usesTitleAscendingPageRequest() {
        String keyword = "Java";
        Long categoryId = 1L;

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("Algorithms Project");

        when(projectTopicRepository.searchTopicsByKeywordAndCategory(
                eq(ProjectTopic.TopicStatus.available),
                eq(keyword),
                eq(categoryId),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(topic)));

        Page<ProjectTopic> result = projectTopicService.searchAvailableTopics(
                keyword,
                categoryId,
                0,
                10,
                "az"
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(projectTopicRepository).searchTopicsByKeywordAndCategory(
                eq(ProjectTopic.TopicStatus.available),
                eq(keyword),
                eq(categoryId),
                argThat(pageable ->
                        pageable.getPageNumber() == 0 &&
                                pageable.getPageSize() == 10 &&
                                pageable.getSort().getOrderFor("title") != null &&
                                pageable.getSort().getOrderFor("title").isAscending()
                )
        );
    }
}
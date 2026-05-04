package com.group24.projectselection.controller;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ProjectTopicService;
import com.group24.projectselection.service.TopicStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectTopicController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectTopicControllerStudentTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectTopicRepository projectTopicRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProjectTopicService projectTopicService;

    @MockBean
    private TopicStatusService topicStatusService;

    @MockBean
    private CategoryRepository categoryRepository;


    @Test
    void studentBrowseAvailableTopics_returnsStudentTopicsPage() throws Exception {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(1L);
        topic.setTitle("test");
        topic.setDescription("1234");
        topic.setRequiredSkills("1234");
        topic.setKeywords("AI");
        topic.setMaxStudents(10);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        Category category = new Category();
        category.setId(1L);
        category.setName("IT");
        category.setIsActive(true);

        when(projectTopicService.searchAvailableTopics(
                eq("java"),
                eq(1L),
                eq(0),
                eq(10),
                eq("newest")
        )).thenReturn(new PageImpl<>(List.of(topic)));
        when(categoryRepository.findByIsActiveTrueOrderByNameAsc())
                .thenReturn(List.of(category));

        mockMvc.perform(get("/student/topics")
                        .param("keyword", "java")
                        .param("category", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student-topics"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("keyword", "java"))
                .andExpect(model().attribute("selectedCategory", 1L));
    }

    @Test
    void studentViewTopicDetail_returnsStudentTopicDetailPage() throws Exception {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(1L);
        topic.setTitle("test");
        topic.setDescription("1234");
        topic.setRequiredSkills("1234");
        topic.setKeywords("AI");
        topic.setMaxStudents(10);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        when(projectTopicRepository.findByIdAndStatus(1L, ProjectTopic.TopicStatus.available))
                .thenReturn(Optional.of(topic));

        mockMvc.perform(get("/student/topics/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("student-topic-detail"))
                .andExpect(model().attributeExists("projectTopic"));
    }

    @Test
    void studentBrowseAvailableTopics_withPageSizeAndSort_returnsPaginatedModel() throws Exception {
        ProjectTopic topic = new ProjectTopic();
        topic.setId(2L);
        topic.setTitle("Algorithms Project");
        topic.setDescription("Description");
        topic.setRequiredSkills("Java");
        topic.setKeywords("algorithm");
        topic.setMaxStudents(10);
        topic.setStatus(ProjectTopic.TopicStatus.available);

        Category category = new Category();
        category.setId(1L);
        category.setName("IT");
        category.setIsActive(true);

        when(projectTopicService.searchAvailableTopics(
                eq("java"),
                eq(1L),
                eq(1),
                eq(10),
                eq("az")
        )).thenReturn(new PageImpl<>(List.of(topic)));

        when(categoryRepository.findByIsActiveTrueOrderByNameAsc())
                .thenReturn(List.of(category));

        mockMvc.perform(get("/student/topics")
                        .param("keyword", "java")
                        .param("category", "1")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "az"))
                .andExpect(status().isOk())
                .andExpect(view().name("student-topics"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("topicPage"))
                .andExpect(model().attribute("keyword", "java"))
                .andExpect(model().attribute("selectedCategory", 1L))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("size", 10))
                .andExpect(model().attribute("sort", "az"));
    }
}
package com.group24.projectselection.controller;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ProjectTopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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

        when(projectTopicRepository.findByStatus(ProjectTopic.TopicStatus.available))
                .thenReturn(List.of(topic));

        mockMvc.perform(get("/student/topics"))
                .andExpect(status().isOk())
                .andExpect(view().name("student-topics"))
                .andExpect(model().attributeExists("projects"));
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
}
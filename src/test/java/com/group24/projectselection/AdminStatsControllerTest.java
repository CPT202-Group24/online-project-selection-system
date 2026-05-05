package com.group24.projectselection;

import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectTopicRepository projectTopicRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void stats_returnsCountsMatchingDatabase() throws Exception {
        long users = userRepository.count();
        long topics = projectTopicRepository.count();
        long apps = applicationRepository.count();

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCount").value(users))
                .andExpect(jsonPath("$.topicCount").value(topics))
                .andExpect(jsonPath("$.applicationCount").value(apps));
    }

    @Test
    @WithMockUser(username = "student@test.com", authorities = {"student"})
    void stats_forbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }
}

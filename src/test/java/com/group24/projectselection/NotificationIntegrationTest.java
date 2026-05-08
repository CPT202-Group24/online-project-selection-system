package com.group24.projectselection;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.Notification;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.repository.NotificationRepository;
import com.group24.projectselection.service.ApplicationService;
import com.group24.projectselection.service.NotificationService;
import com.group24.projectselection.service.TeacherApprovalService;
import com.group24.projectselection.service.TopicStatusService;
import com.group24.projectselection.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Pbi4NotificationIntegrationTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private com.group24.projectselection.service.ProjectTopicService projectTopicService;

    @Autowired
    private TopicStatusService topicStatusService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private TeacherApprovalService teacherApprovalService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void teacherApprovalStatusChange_createsNotificationRecordForStudent() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User teacher = userRegistrationService.register(
                "Teacher PBI4",
                "teacher.pbi4." + suffix + "@xjtlu.edu.cn",
                "secret",
                User.Role.teacher
        );
        User student = userRegistrationService.register(
                "Student PBI4",
                "student.pbi4." + suffix + "@student.xjtlu.edu.cn",
                "secret",
                User.Role.student
        );

        Category category = new Category();
        category.setName("PBI4-CAT-" + suffix);
        category.setDescription("PBI4 category");
        category = categoryRepository.save(category);

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("PBI4 Topic " + suffix);
        topic.setDescription("Notification test topic");
        topic.setRequiredSkills("java");
        topic.setKeywords("pbi4");
        topic.setMaxStudents(2);
        topic.setCategory(category);

        ProjectTopic created = projectTopicService.createProjectTopic(topic, teacher);
        ProjectTopic published = topicStatusService.publishTopic(created.getId(), teacher.getId());

        Application app = applicationService.submitApplication(
                student, published.getId(), "I am interested in this topic."
        );

        teacherApprovalService.processApproval(app.getId(), true, teacher.getId());

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(student.getId());
        assertThat(notifications).isNotEmpty();
        Notification latest = notifications.get(0);
        assertThat(latest.getMessage()).contains("accepted");
        assertThat(latest.getCreatedAt()).isNotNull();
        assertThat(latest.getUser().getId()).isEqualTo(student.getId());
    }

    @Test
    @WithMockUser(username = "student.list@test.com", authorities = {"student"})
    void unreadNotifications_areReturnedLatestFirst() throws Exception {
        User student = userRegistrationService.register(
                "Student List",
                "student.list@test.com",
                "secret",
                User.Role.student
        );

        notificationService.createNotification(student.getId(), "Older message");
        Thread.sleep(1200L);
        notificationService.createNotification(student.getId(), "Newer message");

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Newer message"))
                .andExpect(jsonPath("$[0].isRead").value(false))
                .andExpect(jsonPath("$[1].message").value("Older message"));
    }

    @Test
    @WithMockUser(username = "student.read@test.com", authorities = {"student"})
    void markNotificationRead_updatesUnreadToReadInDatabase() throws Exception {
        User student = userRegistrationService.register(
                "Student Read",
                "student.read@test.com",
                "secret",
                User.Role.student
        );
        Notification notification = notificationService.createNotification(student.getId(), "Please read me");
        assertThat(notification.getIsRead()).isFalse();

        mockMvc.perform(post("/api/notifications/{id}/read", notification.getId()).with(csrf()))
                .andExpect(status().isOk());

        Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
        assertThat(updated.getIsRead()).isTrue();
    }
}

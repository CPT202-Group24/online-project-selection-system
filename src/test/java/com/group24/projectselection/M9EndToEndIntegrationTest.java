package com.group24.projectselection;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.ConflictLog;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.AuditLogRepository;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.repository.ConflictLogRepository;
import com.group24.projectselection.repository.NotificationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ApplicationService;
import com.group24.projectselection.service.CustomUserDetailsService;
import com.group24.projectselection.service.ProjectTopicService;
import com.group24.projectselection.service.TeacherApprovalService;
import com.group24.projectselection.service.TopicStatusService;
import com.group24.projectselection.service.UserRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class M9EndToEndIntegrationTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ProjectTopicService projectTopicService;

    @Autowired
    private TopicStatusService topicStatusService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private TeacherApprovalService teacherApprovalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProjectTopicRepository projectTopicRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ConflictLogRepository conflictLogRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void cleanData() {
        notificationRepository.deleteAll();
        conflictLogRepository.deleteAll();
        applicationRepository.deleteAll();
        projectTopicRepository.deleteAll();
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void fullFlow_registerLoginCreatePublishApplyApprove_ruleFires() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        User teacher = userRegistrationService.register(
                "Teacher M9",
                "teacher." + suffix + "@xjtlu.edu.cn",
                "secret",
                User.Role.teacher
        );

        User studentA = userRegistrationService.register(
                "Student A",
                "student.a." + suffix + "@student.xjtlu.edu.cn",
                "secret",
                User.Role.student
        );

        User studentB = userRegistrationService.register(
                "Student B",
                "student.b." + suffix + "@student.xjtlu.edu.cn",
                "secret",
                User.Role.student
        );

        UserDetails teacherLogin = customUserDetailsService.loadUserByUsername(teacher.getEmail());
        UserDetails studentLogin = customUserDetailsService.loadUserByUsername(studentA.getEmail());

        assertThat(teacherLogin.getAuthorities()).extracting("authority").contains("teacher");
        assertThat(studentLogin.getAuthorities()).extracting("authority").contains("student");

        Category category = new Category();
        category.setName("M9-CAT-" + suffix);
        category.setDescription("Integration category");
        category = categoryRepository.save(category);

        ProjectTopic topic = new ProjectTopic();
        topic.setTitle("M9 Full Flow Topic " + suffix);
        topic.setDescription("Flow test topic");
        topic.setRequiredSkills("java");
        topic.setKeywords("m9,flow");
        topic.setMaxStudents(1);
        topic.setCategory(category);

        ProjectTopic createdTopic = projectTopicService.createProjectTopic(topic, teacher);
        assertThat(createdTopic.getStatus()).isEqualTo(ProjectTopic.TopicStatus.unpublished);

        ProjectTopic publishedTopic = topicStatusService.publishTopic(createdTopic.getId(), teacher.getId());
        assertThat(publishedTopic.getStatus()).isEqualTo(ProjectTopic.TopicStatus.available);

        Application applicationA = applicationService.submitApplication(
                studentA,
                publishedTopic.getId(),
                "I want this project because I match the skills."
        );

        Application applicationB = applicationService.submitApplication(
                studentB,
                publishedTopic.getId(),
                "I also want to join this topic."
        );

        assertThat(applicationA.getStatus()).isEqualTo(Application.ApplicationStatus.pending);
        assertThat(applicationB.getStatus()).isEqualTo(Application.ApplicationStatus.pending);

        teacherApprovalService.processApproval(applicationA.getId(), true);

        Application approvedA = applicationRepository.findById(applicationA.getId()).orElseThrow();
        Application autoRejectedB = applicationRepository.findById(applicationB.getId()).orElseThrow();
        ProjectTopic finalTopic = projectTopicRepository.findById(publishedTopic.getId()).orElseThrow();
        List<ConflictLog> logs = conflictLogRepository.findAll();

        assertThat(approvedA.getStatus()).isEqualTo(Application.ApplicationStatus.accepted);
        assertThat(autoRejectedB.getStatus()).isEqualTo(Application.ApplicationStatus.rejected);
        assertThat(finalTopic.getStatus()).isEqualTo(ProjectTopic.TopicStatus.agreed);

        assertThat(logs)
                .anyMatch(log -> "AUTO_REJECTED".equals(log.getActionTaken())
                        && "Project reached maximum student capacity".equals(log.getReason()));
    }
}
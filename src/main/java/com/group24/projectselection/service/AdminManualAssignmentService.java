package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.Application.ApplicationStatus;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AdminManualAssignmentService {

    private final UserRepository userRepository;
    private final ProjectTopicRepository projectTopicRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    public AdminManualAssignmentService(UserRepository userRepository,
                                        ProjectTopicRepository projectTopicRepository,
                                        ApplicationRepository applicationRepository,
                                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.projectTopicRepository = projectTopicRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
    }

    /** Students who are active and have no accepted application. */
    public List<User> findUnassignedStudents() {
        return userRepository.findActiveStudentsWithNoAcceptedApplication();
    }

    /**
     * Available projects that still have capacity (maxStudents not yet reached).
     * Projects with maxStudents=null are treated as unlimited.
     */
    public List<ProjectTopic> findAssignableProjects() {
        List<ProjectTopic> available =
                projectTopicRepository.findByStatus(ProjectTopic.TopicStatus.available);

        return available.stream().filter(project -> {
            if (project.getMaxStudents() == null) return true;
            long accepted = applicationRepository.findByProjectId(project.getId())
                    .stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.accepted)
                    .count();
            return accepted < project.getMaxStudents();
        }).collect(Collectors.toList());
    }

    /**
     * Manually assign a project to a student.
     * Creates an accepted Application (bypassing the normal apply → approve flow).
     * Auto-rejects the student's other pending applications.
     * Sends a notification to the student.
     */
    @Transactional
    public Application assign(Long studentId, Long projectId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found: " + studentId));

        if (student.getRole() != User.Role.student) {
            throw new IllegalArgumentException("Target user is not a student.");
        }

        // Block if student already has an accepted allocation
        boolean alreadyAccepted = applicationRepository.findByStudentId(studentId).stream()
                .anyMatch(a -> a.getStatus() == ApplicationStatus.accepted);
        if (alreadyAccepted) {
            throw new IllegalStateException(
                    "Student already has an accepted project allocation.");
        }

        ProjectTopic project = projectTopicRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + projectId));

        if (project.getStatus() != ProjectTopic.TopicStatus.available) {
            throw new IllegalStateException(
                    "Only available projects can be manually assigned.");
        }

        // Check capacity
        if (project.getMaxStudents() != null) {
            long acceptedCount = applicationRepository.findByProjectId(projectId).stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.accepted)
                    .count();
            if (acceptedCount >= project.getMaxStudents()) {
                throw new IllegalStateException(
                        "Project has reached its maximum student capacity.");
            }
        }

        // Re-use existing application record if one exists, otherwise create new
        Application application = applicationRepository.findByStudentId(studentId)
                .stream()
                .filter(a -> a.getProject().getId().equals(projectId))
                .findFirst()
                .orElse(null);

        if (application == null) {
            application = new Application();
            application.setStudent(student);
            application.setProject(project);
            application.setPersonalStatement("[Manually assigned by administrator]");
        }
        application.setStatus(ApplicationStatus.accepted);
        Application saved = applicationRepository.save(application);

        // Auto-reject student's other pending applications
        applicationRepository.findByStudentId(studentId).stream()
                .filter(a -> !a.getId().equals(saved.getId())
                        && a.getStatus() == ApplicationStatus.pending)
                .forEach(a -> {
                    a.setStatus(ApplicationStatus.rejected);
                    applicationRepository.save(a);
                });

        // Update project status if now full
        long acceptedAfter = applicationRepository.findByProjectId(projectId).stream()
                .filter(a -> a.getStatus() == ApplicationStatus.accepted)
                .count();
        if (project.getMaxStudents() != null && acceptedAfter >= project.getMaxStudents()) {
            project.setStatus(ProjectTopic.TopicStatus.agreed);
            projectTopicRepository.save(project);
        }

        // Notify student
        notificationService.createNotification(
                studentId,
                "You have been manually assigned to project \""
                        + project.getTitle() + "\" by an administrator.");

        return saved;
    }
}

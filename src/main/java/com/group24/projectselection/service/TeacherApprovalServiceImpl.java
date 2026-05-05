package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.ConflictLog;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ConflictLogRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherApprovalServiceImpl implements TeacherApprovalService {

    private final ApplicationRepository applicationRepository;
    private final ProjectTopicRepository projectTopicRepository;
    private final ConflictLogRepository conflictLogRepository;

    @Autowired
    public TeacherApprovalServiceImpl(
            ApplicationRepository applicationRepository,
            ProjectTopicRepository projectTopicRepository,
            ConflictLogRepository conflictLogRepository) {
        this.applicationRepository = applicationRepository;
        this.projectTopicRepository = projectTopicRepository;
        this.conflictLogRepository = conflictLogRepository;
    }

    @Override
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void processApproval(Long applicationId, boolean isAccepted) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (application.getStatus() != Application.ApplicationStatus.pending) {
            throw new RuntimeException("Only pending applications can be processed");
        }

        ProjectTopic project = application.getProject();

        if (isAccepted) {
            Long studentId = application.getStudent().getId();

            List<Application> studentApplications = applicationRepository.findByStudentId(studentId);
            boolean alreadyAccepted = studentApplications.stream()
                    .anyMatch(app ->
                            !app.getId().equals(applicationId)
                                    && app.getStatus() == Application.ApplicationStatus.accepted
                    );

            if (alreadyAccepted) {
                saveConflictLog(
                        application.getStudent(),
                        project,
                        "APPROVAL_BLOCKED",
                        "Student already has an accepted application"
                );

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Student already has an accepted application"
                );
            }

            List<Application> allApplicationsForProject = applicationRepository.findByProjectId(project.getId());

            long acceptedCountBefore = allApplicationsForProject.stream()
                    .filter(app -> app.getStatus() == Application.ApplicationStatus.accepted)
                    .count();

            if (project.getMaxStudents() != null && acceptedCountBefore >= project.getMaxStudents()) {
                saveConflictLog(
                        application.getStudent(),
                        project,
                        "APPROVAL_BLOCKED",
                        "Project has reached maximum student capacity"
                );

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Project has reached maximum student capacity"
                );
            }

            application.setStatus(Application.ApplicationStatus.accepted);

            for (Application studentApp : studentApplications) {
                if (!studentApp.getId().equals(applicationId)
                        && studentApp.getStatus() == Application.ApplicationStatus.pending) {
                    studentApp.setStatus(Application.ApplicationStatus.rejected);
                    applicationRepository.save(studentApp);

                    saveConflictLog(
                            studentApp.getStudent(),
                            studentApp.getProject(),
                            "AUTO_REJECTED",
                            "Student accepted by another project"
                    );
                }
            }

            long acceptedCountAfter = acceptedCountBefore + 1;
            boolean projectIsFull = project.getMaxStudents() != null
                    && acceptedCountAfter >= project.getMaxStudents();

            if (projectIsFull) {
                project.setStatus(ProjectTopic.TopicStatus.agreed);

                for (Application otherApp : allApplicationsForProject) {
                    if (!otherApp.getId().equals(applicationId)
                            && otherApp.getStatus() == Application.ApplicationStatus.pending) {
                        otherApp.setStatus(Application.ApplicationStatus.rejected);
                        applicationRepository.save(otherApp);

                        saveConflictLog(
                                otherApp.getStudent(),
                                project,
                                "AUTO_REJECTED",
                                "Project reached maximum student capacity"
                        );
                    }
                }
            }
        } else {
            application.setStatus(Application.ApplicationStatus.rejected);
        }

        applicationRepository.save(application);
        projectTopicRepository.save(project);
    }

    @Override
    public List<Application> getAcceptedApplications(Long topicId, Long currentTeacherId) {
        ProjectTopic topic = projectTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        if (!topic.getTeacher().getId().equals(currentTeacherId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this topic.");
        }

        List<Application> allApplications = applicationRepository.findByProjectId(topicId);
        return allApplications.stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.accepted)
                .collect(Collectors.toList());
    }

    private void saveConflictLog(User student, ProjectTopic project, String actionTaken, String reason) {
        if (student == null || project == null) {
            return;
        }

        ConflictLog conflictLog = new ConflictLog();
        conflictLog.setStudent(student);
        conflictLog.setProject(project);
        conflictLog.setActionTaken(actionTaken);
        conflictLog.setReason(reason);

        conflictLogRepository.save(conflictLog);
    }
}
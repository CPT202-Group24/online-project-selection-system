package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeacherApprovalServiceImpl implements TeacherApprovalService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ProjectTopicRepository projectTopicRepository;

    @Override
    @Transactional
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
                throw new RuntimeException("Student already has an accepted application");
            }

            List<Application> allApplicationsForProject = applicationRepository.findByProjectId(project.getId());

            if (project.getMaxStudents() != null) {
                long acceptedCount = allApplicationsForProject.stream()
                        .filter(app -> app.getStatus() == Application.ApplicationStatus.accepted)
                        .count();

                if (acceptedCount >= project.getMaxStudents()) {
                    throw new RuntimeException("Project has reached maximum student capacity");
                }
            }

            application.setStatus(Application.ApplicationStatus.accepted);
            project.setStatus(ProjectTopic.TopicStatus.agreed);

            for (Application otherApp : allApplicationsForProject) {
                if (!otherApp.getId().equals(applicationId)
                        && otherApp.getStatus() == Application.ApplicationStatus.pending) {
                    otherApp.setStatus(Application.ApplicationStatus.rejected);
                    applicationRepository.save(otherApp);
                }
            }
        } else {
            application.setStatus(Application.ApplicationStatus.rejected);
        }

        applicationRepository.save(application);
        projectTopicRepository.save(project);
    }
}
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
            application.setStatus(Application.ApplicationStatus.accepted);
            project.setStatus(ProjectTopic.TopicStatus.agreed);

            List<Application> allApplicationsForProject = applicationRepository.findByProjectId(project.getId());
            for (Application otherApp : allApplicationsForProject) {
                if (!otherApp.getId().equals(applicationId) && otherApp.getStatus() == Application.ApplicationStatus.pending) {
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

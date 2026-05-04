package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ApplicationRepository;
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

    @Autowired
    public TeacherApprovalServiceImpl(ApplicationRepository applicationRepository, ProjectTopicRepository projectTopicRepository) {
        this.applicationRepository = applicationRepository;
        this.projectTopicRepository = projectTopicRepository;
    }

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

            List<Application> conflictingApplications = applicationRepository.findByProjectId(project.getId());
            for (Application otherApp : conflictingApplications) {
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
}

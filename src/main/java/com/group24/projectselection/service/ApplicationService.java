package com.group24.projectselection.service;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.Application.ApplicationStatus;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectTopicRepository projectTopicRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              ProjectTopicRepository projectTopicRepository) {
        this.applicationRepository = applicationRepository;
        this.projectTopicRepository = projectTopicRepository;
    }

    public ProjectTopic getProjectById(Long projectId) {
        Optional<ProjectTopic> result = projectTopicRepository.findById(projectId);
        if (result.isEmpty()) {
            return null;
        }
        return result.get();
    }

    @Transactional
    public Application submitApplication(User student, Long projectId, String personalStatement) {
        ProjectTopic project = getProjectById(projectId);
        if (project == null) {
            throw new IllegalStateException("Project not found.");
        }

        if (project.getStatus() != ProjectTopic.TopicStatus.available) {
            throw new IllegalStateException("This project is not available for application.");
        }

        List<Application> myApplications = applicationRepository.findByStudentId(student.getId());

        for (Application app : myApplications) {
            boolean hasAcceptedApplication = app.getStatus() == ApplicationStatus.accepted;
            boolean hasAgreedProject = app.getProject() != null
                    && app.getProject().getStatus() == ProjectTopic.TopicStatus.agreed;

            if (hasAcceptedApplication || hasAgreedProject) {
                throw new IllegalStateException("You already have an agreed project.");
            }
        }

        for (Application app : myApplications) {
            if (app.getProject().getId().equals(projectId)) {
                if (app.getStatus() == ApplicationStatus.withdrawn
                        || app.getStatus() == ApplicationStatus.rejected) {
                    app.setPersonalStatement(personalStatement);
                    app.setStatus(ApplicationStatus.pending);
                    return applicationRepository.save(app);
                } else {
                    throw new IllegalStateException("You have already applied to this project.");
                }
            }
        }

        Application application = new Application();
        application.setStudent(student);
        application.setProject(project);
        application.setPersonalStatement(personalStatement);
        application.setStatus(ApplicationStatus.pending);

        return applicationRepository.save(application);
    }

    public List<Application> getMyApplications(User student) {
        return applicationRepository.findByStudentId(student.getId());
    }

    @Transactional
    public void withdrawApplication(User student, Long applicationId) {
        Optional<Application> result = applicationRepository.findById(applicationId);
        if (result.isEmpty()) {
            throw new IllegalStateException("Application not found.");
        }

        Application application = result.get();

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new IllegalStateException("You can only withdraw your own applications.");
        }

        if (application.getStatus() != ApplicationStatus.pending) {
            throw new IllegalStateException("Only pending applications can be withdrawn.");
        }

        application.setStatus(ApplicationStatus.withdrawn);
        applicationRepository.save(application);
    }
}
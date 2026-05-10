package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ApplicationRepository;
import com.group24.projectselection.repository.ConflictLogRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class AdminProjectService {

    private final ProjectTopicRepository projectTopicRepository;
    private final ApplicationRepository applicationRepository;
    private final ConflictLogRepository conflictLogRepository;
    private final NotificationService notificationService;

    public AdminProjectService(ProjectTopicRepository projectTopicRepository,
                               ApplicationRepository applicationRepository,
                               ConflictLogRepository conflictLogRepository,
                               NotificationService notificationService) {
        this.projectTopicRepository = projectTopicRepository;
        this.applicationRepository = applicationRepository;
        this.conflictLogRepository = conflictLogRepository;
        this.notificationService = notificationService;
    }

    public List<ProjectTopic> listAll() {
        return projectTopicRepository.findAll();
    }

    @Transactional
    public ProjectTopic forceArchive(Long projectId) {
        ProjectTopic topic = projectTopicRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + projectId));

        if (topic.getStatus() == ProjectTopic.TopicStatus.archived) {
            throw new IllegalStateException("Topic is already archived.");
        }

        topic.setPreviousStatus(topic.getStatus());
        topic.setStatus(ProjectTopic.TopicStatus.archived);
        ProjectTopic saved = projectTopicRepository.save(topic);

        applicationRepository.findByProjectId(projectId).stream()
                .map(application -> application.getStudent())
                .filter(Objects::nonNull)
                .filter(student -> student.getId() != null)
                .distinct()
                .forEach(student -> notificationService.createNotification(
                        student.getId(),
                        "Project \"" + saved.getTitle() + "\" has been archived and is no longer available."
                ));

        return saved;
    }

    @Transactional
    public ProjectTopic restore(Long projectId) {
        ProjectTopic topic = projectTopicRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + projectId));

        if (topic.getStatus() != ProjectTopic.TopicStatus.archived) {
            throw new IllegalStateException("Only archived topics can be restored.");
        }

        ProjectTopic.TopicStatus restoreTo = topic.getPreviousStatus() != null
                ? topic.getPreviousStatus()
                : ProjectTopic.TopicStatus.closed;

        topic.setStatus(restoreTo);
        topic.setPreviousStatus(null);
        return projectTopicRepository.save(topic);
    }

    @Transactional
    public void deletePermanently(Long projectId) {
        ProjectTopic topic = projectTopicRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + projectId));

        conflictLogRepository.deleteByProjectId(projectId);
        applicationRepository.deleteByProjectId(projectId);
        projectTopicRepository.delete(topic);
    }
}

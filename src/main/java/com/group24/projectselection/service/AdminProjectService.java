package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdminProjectService {

    private final ProjectTopicRepository projectTopicRepository;

    public AdminProjectService(ProjectTopicRepository projectTopicRepository) {
        this.projectTopicRepository = projectTopicRepository;
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
        return projectTopicRepository.save(topic);
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
}

package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;

@Service
public class TopicStatusServiceImpl implements TopicStatusService {

    private final ProjectTopicRepository projectTopicRepository;

    public TopicStatusServiceImpl(ProjectTopicRepository projectTopicRepository) {
        this.projectTopicRepository = projectTopicRepository;
    }

    @Override
    public ProjectTopic publishTopic(Long topicId, Long teacherId) {
        ProjectTopic topic = projectTopicRepository
                .findByIdAndTeacherId(topicId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found or access denied"));

        if (topic.getStatus() != ProjectTopic.TopicStatus.unpublished) {
            throw new IllegalStateException("Only unpublished topics can be published");
        }

        topic.setStatus(ProjectTopic.TopicStatus.available);
        topic.setDraft(false);
        return projectTopicRepository.save(topic);
    }

    @Override
    public ProjectTopic closeTopic(Long topicId, Long teacherId) {
        ProjectTopic topic = projectTopicRepository
                .findByIdAndTeacherId(topicId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found or access denied"));

        // only available/requested topics can be closed
        ProjectTopic.TopicStatus status = topic.getStatus();
        if (status == ProjectTopic.TopicStatus.unpublished) {
            throw new IllegalStateException("Only published topics can be closed");
        }
        if (status == ProjectTopic.TopicStatus.closed) {
            throw new IllegalStateException("Topic is already closed");
        }
        if (status == ProjectTopic.TopicStatus.agreed) {
            throw new IllegalStateException("Topic has an agreed allocation and cannot be closed");
        }
        if (status == ProjectTopic.TopicStatus.archived) {
            throw new IllegalStateException("Cannot modify an archived topic");
        }

        topic.setStatus(ProjectTopic.TopicStatus.closed);
        return projectTopicRepository.save(topic);
    }
}
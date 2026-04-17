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
        return projectTopicRepository.save(topic);
    }
}

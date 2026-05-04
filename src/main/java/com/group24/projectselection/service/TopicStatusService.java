package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;

public interface TopicStatusService {

    ProjectTopic publishTopic(Long topicId, Long teacherId);

    ProjectTopic closeTopic(Long topicId, Long teacherId);

    ProjectTopic archiveTopic(Long topicId, Long teacherId);
}

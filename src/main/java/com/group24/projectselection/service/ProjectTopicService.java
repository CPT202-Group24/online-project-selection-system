package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;

import java.util.List;

public interface ProjectTopicService {

    ProjectTopic createProjectTopic(ProjectTopic projectTopic, User currentUser);

    ProjectTopic updateProjectTopic(ProjectTopic projectTopic, Long currentTeacherId);

    ProjectTopic createDraftProject(ProjectTopic projectTopic, User currentUser);

    ProjectTopic saveDraftProject(ProjectTopic projectTopic, Long currentTeacherId);

    void deleteProjectTopic(Long topicId, Long currentTeacherId);

    List<ProjectTopic> searchAvailableTopics(String keyword, Long categoryId);
}
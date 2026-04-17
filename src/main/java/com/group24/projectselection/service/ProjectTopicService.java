package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;

public interface ProjectTopicService {

    ProjectTopic createProjectTopic(ProjectTopic projectTopic, User currentUser);

    ProjectTopic updateProjectTopic(ProjectTopic projectTopic, Long currentTeacherId);
}
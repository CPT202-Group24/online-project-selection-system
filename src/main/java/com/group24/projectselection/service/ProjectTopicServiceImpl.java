package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectTopicServiceImpl implements ProjectTopicService {

    private final ProjectTopicRepository projectTopicRepository;

    public ProjectTopicServiceImpl(ProjectTopicRepository projectTopicRepository) {
        this.projectTopicRepository = projectTopicRepository;
    }

    @Override
    public ProjectTopic createProjectTopic(ProjectTopic projectTopic, User currentUser) {
        projectTopic.setTeacher(currentUser);
        projectTopic.setStatus(ProjectTopic.TopicStatus.unpublished);
        return projectTopicRepository.save(projectTopic);
    }

    @Override
    public ProjectTopic updateProjectTopic(ProjectTopic projectTopic, Long currentTeacherId) {
        ProjectTopic existingProject = projectTopicRepository
                .findByIdAndTeacherId(projectTopic.getId(), currentTeacherId)
                .orElseThrow(() -> new IllegalArgumentException("You cannot modify this project"));

        existingProject.setTitle(projectTopic.getTitle());
        existingProject.setDescription(projectTopic.getDescription());
        existingProject.setRequiredSkills(projectTopic.getRequiredSkills());
        existingProject.setKeywords(projectTopic.getKeywords());
        existingProject.setMaxStudents(projectTopic.getMaxStudents());
        existingProject.setStatus(ProjectTopic.TopicStatus.unpublished);

        return projectTopicRepository.save(existingProject);
    }
}
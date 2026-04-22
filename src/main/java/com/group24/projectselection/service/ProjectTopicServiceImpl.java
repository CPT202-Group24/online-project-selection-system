package com.group24.projectselection.service;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTopicServiceImpl implements ProjectTopicService {

    private final ProjectTopicRepository projectTopicRepository;
    private final CategoryRepository categoryRepository;

    public ProjectTopicServiceImpl(ProjectTopicRepository projectTopicRepository,
                                   CategoryRepository categoryRepository) {
        this.projectTopicRepository = projectTopicRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProjectTopic createProjectTopic(ProjectTopic projectTopic, User currentUser) {
        projectTopic.setTeacher(currentUser);
        projectTopic.setStatus(ProjectTopic.TopicStatus.unpublished);
        projectTopic.setCategory(resolveCategory(projectTopic.getCategory()));
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
        existingProject.setCategory(resolveCategory(projectTopic.getCategory()));
        existingProject.setMaxStudents(projectTopic.getMaxStudents());
        existingProject.setStatus(ProjectTopic.TopicStatus.unpublished);

        return projectTopicRepository.save(existingProject);
    }

    private Category resolveCategory(Category category) {
        if (category == null || category.getId() == null) return null;
        return categoryRepository.findById(category.getId()).orElse(null);
    }

    @Override
    public List<ProjectTopic> searchAvailableTopics(String keyword, Long categoryId) {
        return projectTopicRepository.searchTopicsByKeywordAndCategory(
                ProjectTopic.TopicStatus.available,
                keyword,
                categoryId
        );
    }
}
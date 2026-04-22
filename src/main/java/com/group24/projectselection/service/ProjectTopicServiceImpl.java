package com.group24.projectselection.service;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        validateCategoryForNormalSave(projectTopic);

        projectTopic.setTeacher(currentUser);
        projectTopic.setCategory(resolveCategory(projectTopic.getCategory()));
        projectTopic.setKeywords(normalizeKeywords(projectTopic.getKeywords()));
        projectTopic.setStatus(ProjectTopic.TopicStatus.unpublished);
        projectTopic.setDraft(false);

        return projectTopicRepository.save(projectTopic);
    }

    @Override
    public ProjectTopic updateProjectTopic(ProjectTopic projectTopic, Long currentTeacherId) {
        ProjectTopic existingProject = projectTopicRepository
                .findByIdAndTeacherId(projectTopic.getId(), currentTeacherId)
                .orElseThrow(() -> new IllegalArgumentException("You cannot modify this project"));

        if (existingProject.getStatus() != ProjectTopic.TopicStatus.unpublished) {
            throw new IllegalArgumentException("Only unpublished projects can be edited");
        }

        validateCategoryForNormalSave(projectTopic);

        existingProject.setTitle(projectTopic.getTitle());
        existingProject.setDescription(projectTopic.getDescription());
        existingProject.setRequiredSkills(projectTopic.getRequiredSkills());
        existingProject.setKeywords(normalizeKeywords(projectTopic.getKeywords()));
        existingProject.setCategory(resolveCategory(projectTopic.getCategory()));
        existingProject.setMaxStudents(projectTopic.getMaxStudents());
        existingProject.setStatus(ProjectTopic.TopicStatus.unpublished);
        existingProject.setDraft(false);

        return projectTopicRepository.save(existingProject);
    }

    @Override
    public ProjectTopic createDraftProject(ProjectTopic projectTopic, User currentUser) {
        projectTopic.setTeacher(currentUser);
        projectTopic.setCategory(resolveCategory(projectTopic.getCategory()));
        projectTopic.setKeywords(normalizeKeywords(projectTopic.getKeywords()));
        projectTopic.setStatus(ProjectTopic.TopicStatus.unpublished);
        projectTopic.setDraft(true);

        return projectTopicRepository.save(projectTopic);
    }

    @Override
    public ProjectTopic saveDraftProject(ProjectTopic projectTopic, Long currentTeacherId) {
        ProjectTopic existingProject = projectTopicRepository
                .findByIdAndTeacherId(projectTopic.getId(), currentTeacherId)
                .orElseThrow(() -> new IllegalArgumentException("You cannot modify this project"));

        if (existingProject.getStatus() != ProjectTopic.TopicStatus.unpublished) {
            throw new IllegalArgumentException("Only unpublished projects can be edited");
        }

        existingProject.setTitle(projectTopic.getTitle());
        existingProject.setDescription(projectTopic.getDescription());
        existingProject.setRequiredSkills(projectTopic.getRequiredSkills());
        existingProject.setKeywords(normalizeKeywords(projectTopic.getKeywords()));
        existingProject.setCategory(resolveCategory(projectTopic.getCategory()));
        existingProject.setMaxStudents(projectTopic.getMaxStudents());
        existingProject.setStatus(ProjectTopic.TopicStatus.unpublished);
        existingProject.setDraft(true);

        return projectTopicRepository.save(existingProject);
    }

    @Override
    public List<ProjectTopic> searchAvailableTopics(String keyword, Long categoryId) {
        return projectTopicRepository.searchTopicsByKeywordAndCategory(
                ProjectTopic.TopicStatus.available,
                keyword,
                categoryId
        );
    }

    private void validateCategoryForNormalSave(ProjectTopic projectTopic) {
        if (projectTopic.getCategory() == null || projectTopic.getCategory().getId() == null) {
            throw new IllegalArgumentException("Please select a category before saving.");
        }
    }

    private Category resolveCategory(Category category) {
        if (category == null || category.getId() == null) {
            return null;
        }

        return categoryRepository.findById(category.getId()).orElse(null);
    }

    private String normalizeKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return null;
        }

        List<String> cleanedKeywords = Arrays.stream(keywords.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        if (cleanedKeywords.isEmpty()) {
            return null;
        }

        return String.join(", ", cleanedKeywords);
    }
}
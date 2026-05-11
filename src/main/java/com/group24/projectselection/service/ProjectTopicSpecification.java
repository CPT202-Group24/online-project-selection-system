package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import org.springframework.data.jpa.domain.Specification;

public class ProjectTopicSpecification {

    // OR
    public static Specification<ProjectTopic> singleKeywordMatches(String word) {
        return (root, query, cb) -> {
            String likePattern = "%" + word.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern),
                    cb.like(cb.lower(root.get("requiredSkills")), likePattern),
                    cb.like(cb.lower(root.get("keywords")), likePattern)
            );
        };
    }

    // AND
    public static Specification<ProjectTopic> allKeywordsMatch(String[] words) {
        Specification<ProjectTopic> spec = Specification.where(null);
        for (String word : words) {
            if (!word.isBlank()) {
                spec = spec.and(singleKeywordMatches(word));
            }
        }
        return spec;
    }

    // status condition
    public static Specification<ProjectTopic> statusIn(ProjectTopic.TopicStatus... statuses) {
        return (root, query, cb) -> root.get("status").in((Object[]) statuses);
    }

    // category condition
    public static Specification<ProjectTopic> categoryIs(Long categoryId) {
        return (root, query, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId);
    }
}
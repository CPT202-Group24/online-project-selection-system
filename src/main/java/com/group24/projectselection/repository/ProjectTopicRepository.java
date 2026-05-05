package com.group24.projectselection.repository;

import com.group24.projectselection.model.ProjectTopic;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTopicRepository extends JpaRepository<ProjectTopic, Long> {

    List<ProjectTopic> findByTeacherId(Long teacherId);

    List<ProjectTopic> findByStatus(ProjectTopic.TopicStatus status);

    Optional<ProjectTopic> findByIdAndTeacherId(Long id, Long teacherId);

    List<ProjectTopic> findByTeacherIdAndStatus(Long teacherId, ProjectTopic.TopicStatus status);

    //List<ProjectTopic> findByKeywordsContainingIgnoreCase(String keyword);

    Optional<ProjectTopic> findByIdAndStatus(Long id, ProjectTopic.TopicStatus status);
    @Query("""
SELECT p FROM ProjectTopic p
WHERE p.status = :status
AND (
    :keyword IS NULL OR TRIM(:keyword) = '' OR
    LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    LOWER(p.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
    LOWER(p.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
AND (
    :categoryId IS NULL OR p.category.id = :categoryId
)
""")
    Page<ProjectTopic> searchTopicsByKeywordAndCategory(
            @Param("status") ProjectTopic.TopicStatus status,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}
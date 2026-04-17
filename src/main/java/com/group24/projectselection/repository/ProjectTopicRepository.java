package com.group24.projectselection.repository;

import com.group24.projectselection.model.ProjectTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTopicRepository extends JpaRepository<ProjectTopic, Long> {

    List<ProjectTopic> findByTeacherId(Long teacherId);

    List<ProjectTopic> findByStatus(ProjectTopic.TopicStatus status);

    Optional<ProjectTopic> findByIdAndTeacherId(Long id, Long teacherId);

    List<ProjectTopic> findByTeacherIdAndStatus(Long teacherId, ProjectTopic.TopicStatus status);

    //List<ProjectTopic> findByKeywordsContainingIgnoreCase(String keyword);
}
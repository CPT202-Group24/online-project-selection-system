package com.group24.projectselection.repository;

import com.group24.projectselection.model.ProjectTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectTopicRepository extends JpaRepository<ProjectTopic, Long> {

    List<ProjectTopic> findByTeacherId(Long teacherId);

    List<ProjectTopic> findByStatus(ProjectTopic.TopicStatus status);
}

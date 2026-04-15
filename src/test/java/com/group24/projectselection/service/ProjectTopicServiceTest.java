package com.group24.projectselection.service;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ProjectTopicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectTopicServiceTest {

    @Mock
    private ProjectTopicRepository projectTopicRepository;

    @InjectMocks
    private ProjectTopicServiceImpl projectTopicService;

    @Test
    void createProjectTopic_setsCorrectTeacherAndUnpublishedStatus() {
        User teacher = new User();
        teacher.setId(1L);
        teacher.setEmail("teacher@test.com");

        ProjectTopic projectTopic = new ProjectTopic();
        projectTopic.setTitle("AI Research");
        projectTopic.setDescription("Study AI methods");
        projectTopic.setRequiredSkills("Java, Spring");
        projectTopic.setKeywords("AI, Java");
        projectTopic.setMaxStudents(3);

        when(projectTopicRepository.save(any(ProjectTopic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTopic result = projectTopicService.createProjectTopic(projectTopic, teacher);

        assertEquals(teacher, result.getTeacher());
        assertEquals(1L, result.getTeacher().getId());
        assertEquals(ProjectTopic.TopicStatus.unpublished, result.getStatus());
    }

    @Test
    void updateProjectTopic_calledByDifferentUser_shouldBeRejected() {
        Long differentTeacherId = 2L;

        ProjectTopic updatedProject = new ProjectTopic();
        updatedProject.setId(100L);
        updatedProject.setTitle("Unauthorized Edit");

        when(projectTopicRepository.findByIdAndTeacherId(100L, differentTeacherId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectTopicService.updateProjectTopic(updatedProject, differentTeacherId)
        );

        assertEquals("You cannot modify this project", exception.getMessage());
    }
}
package com.group24.projectselection.controller;

import com.group24.projectselection.repository.CategoryRepository;
import jakarta.validation.Valid;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ProjectTopicService;
import com.group24.projectselection.service.TopicStatusService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ProjectTopicController {

    private final ProjectTopicRepository projectTopicRepository;
    private final UserRepository userRepository;
    private final ProjectTopicService projectTopicService;
    private final TopicStatusService topicStatusService;
    private final CategoryRepository categoryRepository;

    public ProjectTopicController(ProjectTopicRepository projectTopicRepository,
                                  UserRepository userRepository,
                                  ProjectTopicService projectTopicService,
                                  TopicStatusService topicStatusService,
                                  CategoryRepository categoryRepository) {
        this.projectTopicRepository = projectTopicRepository;
        this.userRepository = userRepository;
        this.projectTopicService = projectTopicService;
        this.topicStatusService = topicStatusService;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/teacher/projects")
    public String listProjects(@RequestParam(value = "view", defaultValue = "my") String view,
                               @RequestParam(value = "status", required = false) String status,
                               Authentication authentication,
                               Model model) {

        User currentUser = getCurrentUser(authentication);
        Long currentTeacherId = currentUser.getId();

        List<ProjectTopic> projects;

        if ("all".equalsIgnoreCase(view)) {
            projects = projectTopicRepository.findAll();
        } else if (status != null && !status.isBlank()) {
            try {
                ProjectTopic.TopicStatus ts = ProjectTopic.TopicStatus.valueOf(status);
                projects = projectTopicRepository.findByTeacherIdAndStatus(currentTeacherId, ts);
            } catch (IllegalArgumentException e) {
                projects = projectTopicRepository.findByTeacherId(currentTeacherId);
            }
            view = "my";
        } else {
            projects = projectTopicRepository.findByTeacherId(currentTeacherId);
            view = "my";
        }

        model.addAttribute("projects", projects);
        model.addAttribute("currentTeacherId", currentTeacherId);
        model.addAttribute("view", view);
        model.addAttribute("currentStatus", status != null ? status : "");

        return "projects";
    }

    @GetMapping("/teacher/projects/new")
    public String showCreateForm(Model model) {
        model.addAttribute("projectTopic", new ProjectTopic());
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
        return "project-form";
    }

    @PostMapping("/teacher/projects")
    public String saveProject(@Valid @ModelAttribute("projectTopic") ProjectTopic projectTopic,
                              BindingResult bindingResult,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        Long currentTeacherId = currentUser.getId();

        if (bindingResult.hasErrors()) {
            return "project-form";
        }

        boolean isEdit = projectTopic.getId() != null;

        try {
            if (isEdit) {
                projectTopicService.updateProjectTopic(projectTopic, currentTeacherId);
                redirectAttributes.addFlashAttribute("successMessage", "Save successfully");
            } else {
                projectTopicService.createProjectTopic(projectTopic, currentUser);
                redirectAttributes.addFlashAttribute("successMessage", "Create successfully");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/teacher/projects";
        }

        return "redirect:/teacher/projects";
    }

    @PostMapping("/teacher/projects/{id}/publish")
    public String publishTopic(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        Long teacherId = getCurrentUser(authentication).getId();
        try {
            topicStatusService.publishTopic(id, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Topic published successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/projects";
    }

    @PostMapping("/teacher/projects/{id}/close")
    public String closeTopic(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        Long teacherId = getCurrentUser(authentication).getId();
        try {
            topicStatusService.closeTopic(id, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Topic closed successfully.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/projects";
    }

    @GetMapping("/teacher/projects/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Long currentTeacherId = getCurrentUser(authentication).getId();

        ProjectTopic projectTopic = projectTopicRepository
                .findByIdAndTeacherId(id, currentTeacherId)
                .orElse(null);

        if (projectTopic == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot modify this project");
            return "redirect:/teacher/projects";
        }

        model.addAttribute("projectTopic", projectTopic);
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
        return "project-form";
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Current user not found: " + email));
    }

    @GetMapping("/student/topics")
    public String listAvailableTopics(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) Long categoryId,
            Model model) {

        List<ProjectTopic> topics = projectTopicService.searchAvailableTopics(keyword, categoryId);

        model.addAttribute("projects", topics);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());

        return "student-topics";
    }

    @GetMapping("/student/topics/{id}")
    public String showStudentTopicDetail(@PathVariable Long id,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        ProjectTopic topic = projectTopicRepository
                .findByIdAndStatus(id, ProjectTopic.TopicStatus.available)
                .orElse(null);

        if (topic == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Topic not found or not available.");
            return "redirect:/student/topics";
        }

        model.addAttribute("projectTopic", topic);
        return "student-topic-detail";
    }
}
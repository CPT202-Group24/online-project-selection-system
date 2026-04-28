package com.group24.projectselection.controller;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ProjectTopicService;
import com.group24.projectselection.service.TopicStatusService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
        ProjectTopic projectTopic = new ProjectTopic();
        projectTopic.setCategory(new Category());

        model.addAttribute("projectTopic", projectTopic);
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
        return "project-form";
    }

    @PostMapping("/teacher/projects")
    public String saveProject(@Valid @ModelAttribute("projectTopic") ProjectTopic projectTopic,
                              BindingResult bindingResult,
                              @RequestParam(value = "submitAction", defaultValue = "save") String submitAction,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser(authentication);
        Long currentTeacherId = currentUser.getId();
        boolean isDraftAction = "draft".equalsIgnoreCase(submitAction);
        boolean isEdit = projectTopic.getId() != null;

        if (!isDraftAction && (projectTopic.getCategory() == null || projectTopic.getCategory().getId() == null)) {
            bindingResult.rejectValue("category", "category.empty", "Please select a category before saving.");
        }

        if (bindingResult.hasErrors()) {
            if (projectTopic.getCategory() == null) {
                projectTopic.setCategory(new Category());
            }
            model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
            return "project-form";
        }

        try {
            if (isDraftAction) {
                if (isEdit) {
                    projectTopicService.saveDraftProject(projectTopic, currentTeacherId);
                } else {
                    projectTopicService.createDraftProject(projectTopic, currentUser);
                }
                redirectAttributes.addFlashAttribute("successMessage", "Draft saved successfully");
            } else {
                if (isEdit) {
                    projectTopicService.updateProjectTopic(projectTopic, currentTeacherId);
                    redirectAttributes.addFlashAttribute("successMessage", "Save successfully");
                } else {
                    projectTopicService.createProjectTopic(projectTopic, currentUser);
                    redirectAttributes.addFlashAttribute("successMessage", "Create successfully");
                }
            }
        } catch (IllegalArgumentException e) {
            if (projectTopic.getCategory() == null) {
                projectTopic.setCategory(new Category());
            }
            model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
            model.addAttribute("errorMessage", e.getMessage());
            return "project-form";
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

    @DeleteMapping("/api/topics/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteTopicApi(@PathVariable Long id,
                                                 Authentication authentication) {
        Long teacherId = getCurrentUser(authentication).getId();

        try {
            projectTopicService.deleteProjectTopic(id, teacherId);
            return ResponseEntity.ok("Project topic deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/teacher/projects/{id}/delete")
    public String deleteTopicFromPage(@PathVariable Long id,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        Long teacherId = getCurrentUser(authentication).getId();

        try {
            projectTopicService.deleteProjectTopic(id, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Project topic deleted successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/teacher/projects";
    }
    //Sprint3 -- detailed page
    @GetMapping("/teacher/topics/{id}")
    public String showTeacherTopicDetail(@PathVariable Long id,
                                         Authentication authentication,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        Long currentTeacherId = getCurrentUser(authentication).getId();

        ProjectTopic topic = projectTopicRepository
                .findById(id)
                .orElse(null);

        if (topic == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Topic not found."
            );
            return "redirect:/teacher/projects";
        }

        model.addAttribute("projectTopic", topic);
        model.addAttribute("currentTeacherId", currentTeacherId);

        return "teacher-topic-detail";
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

        if (projectTopic.getStatus() != ProjectTopic.TopicStatus.unpublished) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only unpublished projects can be edited");
            return "redirect:/teacher/projects";
        }

        if (projectTopic.getCategory() == null) {
            projectTopic.setCategory(new Category());
        }

        model.addAttribute("projectTopic", projectTopic);
        model.addAttribute("categories", categoryRepository.findByIsActiveTrueOrderByNameAsc());
        return "project-form";
    }

    @GetMapping("/student/topics")
    public String listAvailableTopics(@RequestParam(value = "keyword", required = false) String keyword,
                                      @RequestParam(value = "category", required = false) Long categoryId,
                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "10") int size,
                                      @RequestParam(value = "sort", defaultValue = "newest") String sort,
                                      Model model) {

        Page<ProjectTopic> topicPage = projectTopicService.searchAvailableTopics(
                keyword,
                categoryId,
                page,
                size,
                sort
        );

        model.addAttribute("projects", topicPage.getContent());
        model.addAttribute("topicPage", topicPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", topicPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);

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

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Current user not found: " + email));
    }
}
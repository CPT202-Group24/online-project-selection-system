package com.group24.projectselection.controller;

import jakarta.validation.Valid;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.ProjectTopicRepository;
import com.group24.projectselection.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Optional;

@Controller
public class ProjectTopicController {

    private final ProjectTopicRepository projectTopicRepository;
    private final UserRepository userRepository;

    public ProjectTopicController(ProjectTopicRepository projectTopicRepository,
                                  UserRepository userRepository) {
        this.projectTopicRepository = projectTopicRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/projects")

    public String listProjects(@RequestParam(value = "keyword", required = false) String keyword,
                               Model model) {

        List<ProjectTopic> projects;
        Long currentTeacherId = 1L;

        if (keyword != null && !keyword.trim().isEmpty()) {
            projects = projectTopicRepository.findByKeywordsContainingIgnoreCase(keyword.trim());
        } else {
            projects = projectTopicRepository.findAll();
        }

        model.addAttribute("projects", projects);
        model.addAttribute("currentTeacherId", currentTeacherId);
        model.addAttribute("keyword", keyword);

        return "projects";
    }
    @GetMapping("/projects/new")
    public String showCreateForm(Model model) {
        model.addAttribute("projectTopic", new ProjectTopic());
        return "project-form";
    }

    @PostMapping("/projects")
    public String saveProject(@Valid @ModelAttribute("projectTopic") ProjectTopic projectTopic,
                              BindingResult bindingResult,
                              @RequestParam("action") String action,
                              RedirectAttributes redirectAttributes) {

        Long currentTeacherId = 1L;

        if (bindingResult.hasErrors() && !"draft".equals(action)) {
            return "project-form";
        }

        boolean isEdit = projectTopic.getId() != null;

        if (isEdit) {
            Optional<ProjectTopic> existingOptional =
                    projectTopicRepository.findByIdAndTeacherId(projectTopic.getId(), currentTeacherId);

            if (existingOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "You cannot modify this project");
                return "redirect:/projects";
            }

            ProjectTopic existingProject = existingOptional.get();

            existingProject.setTitle(projectTopic.getTitle());
            existingProject.setDescription(projectTopic.getDescription());
            existingProject.setRequiredSkills(projectTopic.getRequiredSkills());
            existingProject.setKeywords(projectTopic.getKeywords());
            existingProject.setMaxStudents(projectTopic.getMaxStudents());

            if ("draft".equals(action)) {
                existingProject.setStatus(ProjectTopic.TopicStatus.draft);
            } else {
                if (existingProject.getStatus() == ProjectTopic.TopicStatus.draft) {
                    existingProject.setStatus(ProjectTopic.TopicStatus.unpublished);
                }
            }

            projectTopicRepository.save(existingProject);
            if ("draft".equals(action)) {
                redirectAttributes.addFlashAttribute("successMessage", "Draft saved successfully");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Save successfully");
            }
        } else {
            User teacher = userRepository.findById(currentTeacherId).orElseThrow();
            projectTopic.setTeacher(teacher);

            if ("draft".equals(action)) {
                projectTopic.setStatus(ProjectTopic.TopicStatus.draft);
            } else {
                projectTopic.setStatus(ProjectTopic.TopicStatus.unpublished);
            }

            projectTopicRepository.save(projectTopic);
            if ("draft".equals(action)) {
                redirectAttributes.addFlashAttribute("successMessage", "Draft saved successfully");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Create successfully");
            }
        }

        return "redirect:/projects";
    }

    @GetMapping("/projects/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Long currentTeacherId = 1L;

        ProjectTopic projectTopic = projectTopicRepository
                .findByIdAndTeacherId(id, currentTeacherId)
                .orElse(null);

        if (projectTopic == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot modify this project");
            return "redirect:/projects";
        }

        model.addAttribute("projectTopic", projectTopic);
        return "project-form";
    }
    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        Long currentTeacherId = 1L;

        ProjectTopic projectTopic = projectTopicRepository
                .findByIdAndTeacherId(id, currentTeacherId)
                .orElse(null);

        if (projectTopic == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot modify this project");
            return "redirect:/projects";
        }

        if (projectTopic.getStatus() != ProjectTopic.TopicStatus.unpublished) {
            redirectAttributes.addFlashAttribute("errorMessage", "Published project topics cannot be deleted");
            return "redirect:/projects";
        }

        projectTopicRepository.delete(projectTopic);
        redirectAttributes.addFlashAttribute("successMessage", "Delete successfully");
        return "redirect:/projects";
    }
}
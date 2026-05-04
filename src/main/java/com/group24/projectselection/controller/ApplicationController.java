package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ApplicationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ApplicationController(ApplicationService applicationService,
                                 UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    private User getCurrentStudent(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/apply/{projectId}")
    public String applyForm(@PathVariable Long projectId, Model model,
                            RedirectAttributes redirectAttributes) {
        ProjectTopic project = applicationService.getProjectById(projectId);
        if (project == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Project not found.");
            return "redirect:/student/topics";
        }
        model.addAttribute("project", project);
        return "student-apply";
    }

    @PostMapping("/apply/{projectId}")
    public String submitApplication(@PathVariable Long projectId,
                                    @RequestParam String personalStatement,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        if (personalStatement == null || personalStatement.trim().length() < 50) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Personal statement must be at least 50 characters.");
            return "redirect:/student/applications/apply/" + projectId;
        }

        User student = getCurrentStudent(userDetails);
        try {
            applicationService.submitApplication(student, projectId, personalStatement);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Your application has been submitted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/student/applications/apply/" + projectId;
        }
        return "redirect:/student/applications/my";
    }

    @GetMapping("/my")
    public String myApplications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User student = getCurrentStudent(userDetails);
        List<Application> applications = applicationService.getMyApplications(student);
        model.addAttribute("applications", applications);
        return "student-my-applications";
    }

    @PostMapping("/{id}/withdraw")
    public String withdrawApplication(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        User student = getCurrentStudent(userDetails);
        try {
            applicationService.withdrawApplication(student, id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Application withdrawn successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/student/applications/my";
    }
}

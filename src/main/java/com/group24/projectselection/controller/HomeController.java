package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.service.TeacherApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private TeacherApprovalService teacherApprovalService;

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboardRedirect(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();

        return switch (role) {
            case "admin" -> "redirect:/admin/dashboard";
            case "teacher" -> "redirect:/teacher/dashboard";
            case "student" -> "redirect:/student/dashboard";
            default -> "redirect:/student/dashboard";
        };
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard() {
        return "student-dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        List<Application> pendingApplications = teacherApprovalService.getPendingApplicationsByEmail(email);
        model.addAttribute("pendingApplications", pendingApplications);
        return "teacher-dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin-dashboard";
    }
}
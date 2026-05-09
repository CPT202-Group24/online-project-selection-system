package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.service.TeacherApprovalService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.PrintWriter;
import java.util.List;

@Controller
public class TeacherApprovalController {

    private final TeacherApprovalService teacherApprovalService;

    public TeacherApprovalController(TeacherApprovalService teacherApprovalService) {
        this.teacherApprovalService = teacherApprovalService;
    }

    @GetMapping("/teacher/approvals")
    public String showApprovals(Authentication authentication, Model model) {
        String email = authentication.getName();
        List<Application> applications = teacherApprovalService.getPendingApplicationsByEmail(email);

        model.addAttribute("applications", applications);
        model.addAttribute("pendingTotal", applications.size());

        return "teacher-approvals";
    }

    @PostMapping("/teacher/approvals/{id}/accept")
    public String acceptApplication(@PathVariable Long id,
                                    @RequestParam(value = "redirect", required = false) String redirect,
                                    Authentication authentication,
                                    RedirectAttributes ra) {
        try {
            teacherApprovalService.processApprovalByTeacherEmail(id, true, authentication.getName());
            ra.addFlashAttribute("successMessage", "Application approved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:" + safeRedirect(redirect);
    }

    @PostMapping("/teacher/approvals/{id}/reject")
    public String rejectApplication(@PathVariable Long id,
                                    @RequestParam(value = "redirect", required = false) String redirect,
                                    Authentication authentication,
                                    RedirectAttributes ra) {
        try {
            teacherApprovalService.processApprovalByTeacherEmail(id, false, authentication.getName());
            ra.addFlashAttribute("successMessage", "Application rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:" + safeRedirect(redirect);
    }

    @PostMapping("/api/teacher/applications/{id}/approve")
    public String approveApplication(@PathVariable Long id,
                                     Authentication authentication,
                                     RedirectAttributes ra) {
        try {
            teacherApprovalService.processApprovalByTeacherEmail(id, true, authentication.getName());
            ra.addFlashAttribute("successMessage", "Application approved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/api/teacher/applications/{id}/reject")
    public String rejectApplicationApi(@PathVariable Long id,
                                       Authentication authentication,
                                       RedirectAttributes ra) {
        try {
            teacherApprovalService.processApprovalByTeacherEmail(id, false, authentication.getName());
            ra.addFlashAttribute("successMessage", "Application rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/teacher/applications/{id}/approve")
    public String approveApplicationFromTopic(@PathVariable Long id,
                                              @RequestParam(value = "topicId", required = false) Long topicId,
                                              Authentication authentication,
                                              RedirectAttributes ra) {
        try {
            teacherApprovalService.processApprovalByTeacherEmail(id, true, authentication.getName());
            ra.addFlashAttribute("successMessage", "Application approved successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + safeTopicRedirect(topicId);
    }

    @PostMapping("/teacher/applications/{id}/reject")
    public String rejectApplicationFromTopic(@PathVariable Long id,
                                             @RequestParam(value = "topicId", required = false) Long topicId,
                                             Authentication authentication,
                                             RedirectAttributes ra) {
        try {
            teacherApprovalService.processApprovalByTeacherEmail(id, false, authentication.getName());
            ra.addFlashAttribute("successMessage", "Application rejected.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:" + safeTopicRedirect(topicId);
    }

    @GetMapping({
            "/api/teacher/applications/topics/{topicId}/accepted",
            "/api/teacher/applications/topics/{topicId}/students"
    })
    public ResponseEntity<List<Application>> getAcceptedApplications(
            @PathVariable("topicId") Long topicId,
            Authentication authentication) {
        try {
            List<Application> acceptedApps =
                    teacherApprovalService.getAcceptedApplicationsByTeacherEmail(topicId, authentication.getName());
            return ResponseEntity.ok(acceptedApps);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/api/teacher/applications/topics/{topicId}/export-csv")
    public void exportAcceptedStudentsCsv(
            @PathVariable("topicId") Long topicId,
            Authentication authentication,
            HttpServletResponse response) throws Exception {

        List<Application> acceptedApps =
                teacherApprovalService.getAcceptedApplicationsByTeacherEmail(topicId, authentication.getName());

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"accepted_students_topic_" + topicId + ".csv\""
        );

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("Student Name,Student Email");

        for (Application app : acceptedApps) {
            String studentName = app.getStudent() != null ? app.getStudent().getName() : "N/A";
            String studentEmail = app.getStudent() != null ? app.getStudent().getEmail() : "N/A";
            writer.println(studentName + "," + studentEmail);
        }

        writer.flush();
        writer.close();
    }

    private String safeRedirect(String redirect) {
        if ("/teacher/dashboard".equals(redirect)) {
            return "/teacher/dashboard";
        }

        return "/teacher/approvals";
    }

    private String safeTopicRedirect(Long topicId) {
        if (topicId == null || topicId <= 0) {
            return "/teacher/projects";
        }

        return "/teacher/topics/" + topicId;
    }
}

package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.service.TeacherApprovalService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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

    @PostMapping("/api/teacher/applications/{id}/approve")
    @ResponseBody
    public ResponseEntity<String> approveApplicationApi(@PathVariable Long id,
                                                        @RequestParam(value = "teacherId", required = false) Long currentTeacherId) {
        try {
            teacherApprovalService.processApproval(id, true, currentTeacherId);
            return ResponseEntity.ok("Application has been APPROVED successfully.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Error: " + e.getReason());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/api/teacher/applications/{id}/reject")
    @ResponseBody
    public ResponseEntity<String> rejectApplicationApi(@PathVariable Long id,
                                                       @RequestParam(value = "teacherId", required = false) Long currentTeacherId) {
        try {
            teacherApprovalService.processApproval(id, false, currentTeacherId);
            return ResponseEntity.ok("Application has been REJECTED.");
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body("Error: " + e.getReason());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/teacher/applications/{id}/approve")
    public String approveApplicationFromPage(@PathVariable Long id,
                                             @RequestParam("topicId") Long topicId,
                                             @RequestParam("teacherId") Long currentTeacherId,
                                             RedirectAttributes redirectAttributes) {
        try {
            teacherApprovalService.processApproval(id, true, currentTeacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Application approved successfully.");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/topics/" + topicId;
    }

    @PostMapping("/teacher/applications/{id}/reject")
    public String rejectApplicationFromPage(@PathVariable Long id,
                                            @RequestParam("topicId") Long topicId,
                                            @RequestParam("teacherId") Long currentTeacherId,
                                            RedirectAttributes redirectAttributes) {
        try {
            teacherApprovalService.processApproval(id, false, currentTeacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Application rejected successfully.");
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getReason());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/teacher/topics/" + topicId;
    }

    @GetMapping("/api/teacher/applications/topics/{topicId}/students")
    @ResponseBody
    public ResponseEntity<List<Application>> viewAcceptedStudents(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId) {
        try {
            List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);
            return ResponseEntity.ok(acceptedApps);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @GetMapping("/api/teacher/applications/topics/{topicId}/export-csv")
    public void exportAcceptedStudentsCsv(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId,
            HttpServletResponse response) throws Exception {

        List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"accepted_students_topic_" + topicId + ".csv\"");

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
}

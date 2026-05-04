package com.group24.projectselection.controller;

import com.group24.projectselection.service.TeacherApprovalService;
import com.group24.projectselection.model.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/applications")
public class TeacherApprovalController {

    @Autowired
    private TeacherApprovalService teacherApprovalService;

    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveApplication(@PathVariable Long id) {
        try {
            teacherApprovalService.processApproval(id, true);
            return ResponseEntity.ok("Application has been APPROVED successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectApplication(@PathVariable Long id) {
        try {
            teacherApprovalService.processApproval(id, false);
            return ResponseEntity.ok("Application has been REJECTED.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/topics/{topicId}/students")
    public ResponseEntity<List<Application>> viewAcceptedStudents(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId) {

        List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);
        return ResponseEntity.ok(acceptedApps);
    }

    @GetMapping("/topics/{topicId}/export-csv")
    public void exportAcceptedStudentsCsv(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId,
            HttpServletResponse response) throws Exception {

        List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"accepted_students_topic_" + topicId + ".csv\"");

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("Student Name,Student ID");

        for (Application app : acceptedApps) {
            String studentName = app.getStudent().getName();
            String studentEmail = app.getStudent().getEmail();
            writer.println(studentName + "," + studentEmail);
        }

        writer.flush();
        writer.close();
    }
}

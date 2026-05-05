package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.service.TeacherApprovalService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/applications")
public class TeacherApprovalController {

    @Autowired
    private TeacherApprovalService teacherApprovalService;

    @GetMapping("/topics/{topicId}/students")
    public ResponseEntity<?> viewAcceptedStudents(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId) {
        try {
            List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);
            return ResponseEntity.ok(acceptedApps);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: " + e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/topics/{topicId}/students/export")
    public void exportAcceptedStudentsCsv(
            @PathVariable("topicId") Long topicId,
            @RequestParam("teacherId") Long currentTeacherId,
            HttpServletResponse response) throws IOException {
        try {
            List<Application> acceptedApps = teacherApprovalService.getAcceptedApplications(topicId, currentTeacherId);
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"accepted_students_topic_" + topicId + ".csv\"");
            PrintWriter writer = response.getWriter();
            writer.write('\ufeff');
            writer.println("Student Name,Student Email");
            for (Application app : acceptedApps) {
                String studentName = app.getStudent().getName();
                String studentEmail = app.getStudent().getEmail();
                writer.println(studentName + "," + studentEmail);
            }
            writer.flush();
        } catch (ResponseStatusException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: " + e.getReason());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error: " + e.getMessage());
        }
    }
}
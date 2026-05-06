package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.AdminManualAssignmentService;
import com.group24.projectselection.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class AdminManualAssignmentController {

    private final AdminManualAssignmentService assignmentService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminManualAssignmentController(AdminManualAssignmentService assignmentService,
                                           UserRepository userRepository,
                                           AuditLogService auditLogService) {
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    /** Page */
    @GetMapping("/admin/assignments")
    public String assignmentsPage(Model model) {
        model.addAttribute("unassignedStudents", assignmentService.findUnassignedStudents());
        model.addAttribute("assignableProjects", assignmentService.findAssignableProjects());
        return "admin-assignments";
    }

    /** REST: list unassigned students */
    @GetMapping("/api/admin/unassigned-students")
    @ResponseBody
    public List<Map<String, Object>> listUnassignedStudents() {
        return assignmentService.findUnassignedStudents().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "name", u.getName() != null ? u.getName() : "",
                        "email", u.getEmail()))
                .toList();
    }

    /** REST: list projects available for assignment */
    @GetMapping("/api/admin/available-projects-for-assignment")
    @ResponseBody
    public List<Map<String, Object>> listAssignableProjects() {
        return assignmentService.findAssignableProjects().stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "title", p.getTitle() != null ? p.getTitle() : "(No title)",
                        "teacherName", p.getTeacher() != null ? p.getTeacher().getName() : "N/A",
                        "maxStudents", p.getMaxStudents() != null ? p.getMaxStudents() : "Unlimited"))
                .toList();
    }

    /** REST: perform manual assignment */
    @PostMapping("/api/admin/assignments")
    @ResponseBody
    public ResponseEntity<?> assign(@RequestBody Map<String, Long> body,
                                    Authentication authentication) {
        Long studentId = body.get("studentId");
        Long projectId = body.get("projectId");

        if (studentId == null || projectId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "studentId and projectId are required."));
        }

        try {
            Application result = assignmentService.assign(studentId, projectId);

            User admin = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (admin != null) {
                auditLogService.log(admin,
                        AuditLogService.ACTION_ADMIN_MANUAL_ASSIGN,
                        AuditLogService.ENTITY_USER,
                        studentId);
            }

            String projectTitle = result.getProject() != null ? result.getProject().getTitle() : "";
            String studentName  = result.getStudent()  != null ? result.getStudent().getName()  : "";
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully assigned \"" + projectTitle
                            + "\" to student " + studentName + ".",
                    "applicationId", result.getId()
            ));

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        }
    }
}

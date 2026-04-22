package com.group24.projectselection.controller;

import com.group24.projectselection.service.TeacherApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}

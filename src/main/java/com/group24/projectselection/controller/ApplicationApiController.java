package com.group24.projectselection.controller;

import com.group24.projectselection.model.Application;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.ApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationApiController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    public ApplicationApiController(ApplicationService applicationService,
                                    UserRepository userRepository) {
        this.applicationService = applicationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<String> submitApplication(@RequestParam Long projectId,
                                                    @RequestParam String personalStatement,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (personalStatement == null || personalStatement.trim().length() < 50) {
            return ResponseEntity.badRequest().body("Personal statement must be at least 50 characters.");
        }

        String email = userDetails.getUsername();
        User student = userRepository.findByEmail(email).orElse(null);
        if (student == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        try {
            applicationService.submitApplication(student, projectId, personalStatement);
            return ResponseEntity.ok("Application submitted successfully.");
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("already applied")) {
                return ResponseEntity.status(409).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

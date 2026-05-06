package com.group24.projectselection.controller;

import com.group24.projectselection.model.ProjectTopic;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.AdminProjectService;
import com.group24.projectselection.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class AdminProjectController {

    private final AdminProjectService adminProjectService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminProjectController(AdminProjectService adminProjectService,
                                  UserRepository userRepository,
                                  AuditLogService auditLogService) {
        this.adminProjectService = adminProjectService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/admin/projects")
    public String adminProjectsPage(Model model) {
        return "admin-projects";
    }

    @GetMapping("/api/admin/projects")
    @ResponseBody
    public List<Map<String, Object>> listAllProjects() {
        return adminProjectService.listAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("title", p.getTitle() != null ? p.getTitle() : "(No title)");
            m.put("teacherName", p.getTeacher() != null ? p.getTeacher().getName() : "N/A");
            m.put("category", p.getCategory() != null ? p.getCategory().getName() : "N/A");
            m.put("maxStudents", p.getMaxStudents() != null ? p.getMaxStudents() : "—");
            m.put("status", p.getStatus().name());
            return m;
        }).toList();
    }

    @PostMapping("/api/admin/projects/{id}/archive")
    @ResponseBody
    public ResponseEntity<?> forceArchive(@PathVariable Long id, Authentication authentication) {
        try {
            ProjectTopic topic = adminProjectService.forceArchive(id);
            logAction(authentication, AuditLogService.ACTION_PROJECT_FORCE_ARCHIVE, id);
            return ResponseEntity.ok(Map.of("status", topic.getStatus().name()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/projects/{id}/restore")
    @ResponseBody
    public ResponseEntity<?> restore(@PathVariable Long id, Authentication authentication) {
        try {
            ProjectTopic topic = adminProjectService.restore(id);
            logAction(authentication, AuditLogService.ACTION_PROJECT_RESTORE, id);
            return ResponseEntity.ok(Map.of("status", topic.getStatus().name()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void logAction(Authentication authentication, String action, Long entityId) {
        if (authentication != null && authentication.isAuthenticated()) {
            User admin = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (admin != null) {
                auditLogService.log(admin, action,
                        AuditLogService.ENTITY_PROJECT_TOPIC, entityId);
            }
        }
    }
}

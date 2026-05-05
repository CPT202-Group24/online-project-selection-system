package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.AuditLogService;
import com.group24.projectselection.service.UserAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class AdminUserController {

    private final UserAdminService userAdminService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminUserController(UserAdminService userAdminService,
                               UserRepository userRepository,
                               AuditLogService auditLogService) {
        this.userAdminService = userAdminService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/admin/users")
    public String usersPage() {
        return "admin-users";
    }

    @GetMapping("/api/admin/users")
    @ResponseBody
    public Object listUsers() {
        return userAdminService.listAllUsers();
    }

    @PutMapping("/api/admin/users/{id}/status")
    @ResponseBody
    public ResponseEntity<?> toggleStatus(@PathVariable Long id, Authentication authentication) {
        try {
            return ResponseEntity.ok(userAdminService.toggleStatus(id, authentication.getName()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/users/{id}/role")
    @ResponseBody
    public ResponseEntity<?> changeRole(@PathVariable Long id,
                                        @RequestBody Map<String, String> body,
                                        Authentication authentication) {
        try {
            UserAdminService.UserSummary result =
                    userAdminService.updateRole(id, body.get("role"), authentication.getName());
            User admin = userRepository.findByEmail(authentication.getName()).orElse(null);
            if (admin != null) {
                auditLogService.log(admin, AuditLogService.ACTION_USER_ROLE_CHANGE,
                        AuditLogService.ENTITY_USER, id);
            }
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

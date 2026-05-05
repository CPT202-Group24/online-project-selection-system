package com.group24.projectselection.controller;

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

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
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
            return ResponseEntity.ok(
                    userAdminService.updateRole(id, body.get("role"), authentication.getName())
            );
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

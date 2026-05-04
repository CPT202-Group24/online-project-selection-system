package com.group24.projectselection.controller;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.AuditLogService;
import com.group24.projectselection.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminCategoryController(CategoryService categoryService,
                                   UserRepository userRepository,
                                   AuditLogService auditLogService) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    /** HTML page route — GET /admin/categories */
    @GetMapping("/admin/categories")
    public String categoriesPage() {
        return "admin-categories";
    }

    /** REST: list categories with simple pagination */
    @GetMapping("/api/admin/categories")
    @ResponseBody
    public Map<String, Object> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Category> result = categoryService.findPage(page, size);
        return Map.of(
                "items", result.getContent(),
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalItems", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
    }

    /** REST: create a new category */
    @PostMapping("/api/admin/categories")
    @ResponseBody
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String description = body.get("description");
        try {
            Category created = categoryService.create(name, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** REST: delete a category by id */
    @DeleteMapping("/api/admin/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable Long id, Authentication authentication) {
        try {
            categoryService.delete(id);
            if (authentication != null && authentication.isAuthenticated()) {
                User admin = userRepository.findByEmail(authentication.getName()).orElse(null);
                if (admin != null) {
                    auditLogService.log(
                            admin,
                            AuditLogService.ACTION_CATEGORY_DELETE,
                            AuditLogService.ENTITY_CATEGORY,
                            id);
                }
            }
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

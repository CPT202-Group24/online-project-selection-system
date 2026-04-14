package com.group24.projectselection.controller;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** HTML page route — GET /admin/categories */
    @GetMapping("/admin/categories")
    public String categoriesPage() {
        return "admin-categories";
    }

    /** REST: list all categories ordered by name */
    @GetMapping("/api/admin/categories")
    @ResponseBody
    public List<Category> listCategories() {
        return categoryService.findAll();
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
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

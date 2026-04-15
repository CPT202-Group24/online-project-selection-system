package com.group24.projectselection.config;

import com.group24.projectselection.service.CategoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the three required test categories (IT / Science / Business) on first
 * startup.  Runs only when the categories table is completely empty, so it is
 * safe to restart the application without producing duplicates.
 */
@Component
public class CategoryDataSeeder implements CommandLineRunner {

    private final CategoryService categoryService;

    public CategoryDataSeeder(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {
        if (categoryService.count() == 0) {
            categoryService.create("IT",       "Information Technology related projects");
            categoryService.create("Science",  "Natural and applied science projects");
            categoryService.create("Business", "Business and management projects");
        }
    }
}

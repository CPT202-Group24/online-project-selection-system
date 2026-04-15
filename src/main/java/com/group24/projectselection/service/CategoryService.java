package com.group24.projectselection.service;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public long count() {
        return categoryRepository.count();
    }

    @Transactional
    public Category create(String name, String description) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Category name must not be blank.");
        }
        String trimmedName = name.trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
        Category category = new Category();
        category.setName(trimmedName);
        category.setDescription(StringUtils.hasText(description) ? description.trim() : null);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NoSuchElementException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}

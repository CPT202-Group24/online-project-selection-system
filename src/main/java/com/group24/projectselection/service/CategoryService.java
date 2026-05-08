package com.group24.projectselection.service;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.service.shared.CrudResult;
import com.group24.projectselection.service.shared.SharedDataAccessUtility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final CategoryRepository categoryRepository;
    private final SharedDataAccessUtility dataAccessUtility;

    public CategoryService(CategoryRepository categoryRepository,
                           SharedDataAccessUtility dataAccessUtility) {
        this.categoryRepository = categoryRepository;
        this.dataAccessUtility = dataAccessUtility;
    }

    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Page<Category> findPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("name").ascending());
        return categoryRepository.findAll(pageRequest);
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
        CrudResult<Category> result = dataAccessUtility.create(categoryRepository, category, "Category");
        if (!result.success()) {
            throw new IllegalStateException(result.message());
        }
        return result.data();
    }

    @Transactional
    public Category update(Long id, String name, String description) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Category name must not be blank.");
        }
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + id));
        String trimmed = name.trim();
        if (!trimmed.equalsIgnoreCase(category.getName())
                && categoryRepository.existsByNameIgnoreCase(trimmed)) {
            throw new IllegalArgumentException("A category with this name already exists.");
        }
        category.setName(trimmed);
        category.setDescription(StringUtils.hasText(description) ? description.trim() : null);
        CrudResult<Category> result = dataAccessUtility.update(categoryRepository, category, "Category");
        if (!result.success()) {
            throw new IllegalStateException(result.message());
        }
        return result.data();
    }

    @Transactional
    public Category deactivate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + id));
        category.setIsActive(false);
        CrudResult<Category> result = dataAccessUtility.update(categoryRepository, category, "Category");
        if (!result.success()) {
            throw new IllegalStateException(result.message());
        }
        return result.data();
    }

    @Transactional
    public Category activate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + id));
        category.setIsActive(true);
        CrudResult<Category> result = dataAccessUtility.update(categoryRepository, category, "Category");
        if (!result.success()) {
            throw new IllegalStateException(result.message());
        }
        return result.data();
    }

    @Transactional
    public void delete(Long id) {
        CrudResult<Void> result = dataAccessUtility.deleteById(categoryRepository, id, "Category");
        if (!result.success()) {
            if (result.statusCode() == 404) {
                throw new NoSuchElementException("Category not found: " + id);
            }
            throw new IllegalStateException(result.message());
        }
    }
}

package com.group24.projectselection.service;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.service.shared.CrudResult;
import com.group24.projectselection.service.shared.SharedDataAccessUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SharedDataAccessUtilityTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SharedDataAccessUtility utility;

    @Test
    void create_success_returnsCreatedStructure() {
        Category category = new Category();
        category.setName("IT");

        Category saved = new Category();
        saved.setId(1L);
        saved.setName("IT");

        when(categoryRepository.save(category)).thenReturn(saved);

        CrudResult<Category> result = utility.create(categoryRepository, category, "Category");

        assertThat(result.success()).isTrue();
        assertThat(result.statusCode()).isEqualTo(201);
        assertThat(result.data()).isNotNull();
        assertThat(result.data().getId()).isEqualTo(1L);
    }

    @Test
    void readById_notFound_returns404Structure() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        CrudResult<Category> result = utility.readById(categoryRepository, 99L, "Category");

        assertThat(result.success()).isFalse();
        assertThat(result.statusCode()).isEqualTo(404);
        assertThat(result.message()).contains("not found");
    }

    @Test
    void create_repositoryError_returns500Structure() {
        Category category = new Category();
        category.setName("IT");

        doThrow(new RuntimeException("db error")).when(categoryRepository).save(any(Category.class));

        CrudResult<Category> result = utility.create(categoryRepository, category, "Category");

        assertThat(result.success()).isFalse();
        assertThat(result.statusCode()).isEqualTo(500);
        assertThat(result.message()).contains("Failed to create");
    }
}

package com.group24.projectselection;

import com.group24.projectselection.model.Category;
import com.group24.projectselection.repository.CategoryRepository;
import com.group24.projectselection.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_withValidInput_shouldSucceed() {
        Category saved = new Category();
        saved.setId(1L);
        saved.setName("IT");
        saved.setDescription("Information Technology");

        when(categoryRepository.existsByNameIgnoreCase("IT")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        Category result = categoryService.create("IT", "Information Technology");

        assertEquals("IT", result.getName());
        assertEquals("Information Technology", result.getDescription());
    }

    @Test
    void create_withDuplicateName_shouldThrowException() {
        when(categoryRepository.existsByNameIgnoreCase("IT")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> categoryService.create("IT", "duplicate"));
    }
}

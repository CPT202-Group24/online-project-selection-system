package com.group24.projectselection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group24.projectselection.model.Category;
import com.group24.projectselection.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearCategories() {
        categoryRepository.deleteAll();
    }

    /**
     * PBI 8.2 Test 1: POST /api/admin/categories — category created and returned in the list.
     */
    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void createCategory_succeeds_and_is_persisted() throws Exception {
        Map<String, String> body = Map.of("name", "TestCategory", "description", "A test category");

        mockMvc.perform(post("/api/admin/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("TestCategory"))
                .andExpect(jsonPath("$.isActive").value(true));

        assertThat(categoryRepository.existsByNameIgnoreCase("TestCategory")).isTrue();
    }


    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void listCategories_supportsPagination() throws Exception {
        for (int i = 1; i <= 7; i++) {
            Map<String, String> body = Map.of("name", "Category" + i, "description", "D" + i);
            mockMvc.perform(post("/api/admin/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/admin/categories")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalItems").value(7))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    /**
     * PBI 8.2 Test 2: DELETE /api/admin/categories/{id} — delete removes it from the list.
     */
    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"admin"})
    void deleteCategory_removesItFromRepository() throws Exception {
        // Create directly via API first
        Map<String, String> body = Map.of("name", "ToDelete", "description", "");

        String responseJson = mockMvc.perform(post("/api/admin/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(responseJson, Category.class).getId();
        assertThat(categoryRepository.existsById(id)).isTrue();

        mockMvc.perform(delete("/api/admin/categories/" + id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.existsById(id)).isFalse();
    }
}

package com.group24.projectselection.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecureLogoutTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String STUDENT_EMAIL = "student1@student.xjtlu.edu.cn";

    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = STUDENT_EMAIL;
    }

    @AfterEach
    void tearDown() {
        testEmail = null;
    }

    @Test
    @DisplayName("POST /logout clears session and redirects to login page")
    void logout_clearsSessionAndRedirects() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/logout")
                        .with(user(testEmail).authorities(() -> "student"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse();

        assertEquals(302, response.getStatus());
        assertNotNull(response.getRedirectedUrl());
        assertTrue(response.getRedirectedUrl().contains("/login?logout=true"));
    }

    @Test
    @DisplayName("After logout, accessing a protected page redirects to login")
    void afterLogout_protectedPageRequiresLogin() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse();

        assertEquals(302, response.getStatus());
        assertTrue(response.getRedirectedUrl().contains("/login"));
    }

    @Test
    @DisplayName("Protected pages include Cache-Control headers to prevent back-button access")
    void protectedPages_haveCacheControlHeaders() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(get("/student/dashboard")
                        .with(user(testEmail).authorities(() -> "student")))
                .andReturn().getResponse();

        String cacheControl = response.getHeader("Cache-Control");
        assertNotNull(cacheControl, "Cache-Control header should be present");
        assertTrue(cacheControl.contains("no-cache") || cacheControl.contains("no-store"),
                "Cache-Control should prevent caching");
    }
}

package com.group24.projectselection;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalErrorHandlingIntegrationTest.ThrowingEndpointConfig.class)
class GlobalErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    @WithMockUser(username = "student@test.com", authorities = {"student"})
    void accessDenied_shouldRender403Page() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    @WithMockUser(username = "student@test.com", authorities = {"student"})
    void unknownPath_shouldRender404Page() throws Exception {
        mockMvc.perform(get("/path-that-does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    @WithMockUser(username = "student@test.com", authorities = {"student"})
    void unhandledException_shouldRender500Page() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"));
    }

    @TestConfiguration
    static class ThrowingEndpointConfig {
        @Bean
        ThrowingTestController throwingTestController() {
            return new ThrowingTestController();
        }
    }

    @Controller
    static class ThrowingTestController {
        @GetMapping("/test/boom")
        String boom() {
            throw new RuntimeException("boom");
        }
    }
}

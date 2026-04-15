package com.group24.projectselection.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleBasedAuthenticationSuccessHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private RoleBasedAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RoleBasedAuthenticationSuccessHandler();
        when(request.getContextPath()).thenReturn("");
    }

    @ParameterizedTest
    @CsvSource({
            "admin, /admin/dashboard",
            "teacher, /teacher/dashboard",
            "student, /student/dashboard"
    })
    void onAuthenticationSuccess_redirectsToDashboardForRole(String authority, String expectedPath)
            throws IOException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@test.edu.cn",
                null,
                List.of(new SimpleGrantedAuthority(authority)));

        handler.onAuthenticationSuccess(request, response, auth);

        ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(location.capture());
        assertThat(location.getValue()).isEqualTo(expectedPath);
    }
}

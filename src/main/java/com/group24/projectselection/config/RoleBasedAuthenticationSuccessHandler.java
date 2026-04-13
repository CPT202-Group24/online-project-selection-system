package com.group24.projectselection.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String path = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> pathForRole(a.getAuthority()))
                .orElse("/student/dashboard");
        response.sendRedirect(request.getContextPath() + path);
    }

    private String pathForRole(String authority) {
        return switch (authority) {
            case "admin" -> "/admin/dashboard";
            case "teacher" -> "/teacher/dashboard";
            case "student" -> "/student/dashboard";
            default -> "/student/dashboard";
        };
    }
}

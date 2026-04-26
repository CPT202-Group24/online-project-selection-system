package com.group24.projectselection.service;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void toggleStatus_validTargetUser_shouldSucceed() {
        User target = new User();
        target.setId(1001L);
        target.setEmail("student1@test.com");
        target.setName("Student One");
        target.setRole(User.Role.student);
        target.setStatus(User.UserStatus.active);

        when(userRepository.findById(1001L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        UserAdminService.UserSummary result = userAdminService.toggleStatus(1001L, "admin@test.com");

        assertEquals("disabled", result.status());
    }

    @Test
    void toggleStatus_selfDisable_shouldThrowException() {
        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setName("Admin");
        admin.setRole(User.Role.admin);
        admin.setStatus(User.UserStatus.active);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(IllegalArgumentException.class,
                () -> userAdminService.toggleStatus(1L, "admin@test.com"));
    }
}

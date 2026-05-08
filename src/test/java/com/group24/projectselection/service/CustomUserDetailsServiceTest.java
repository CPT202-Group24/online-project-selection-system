package com.group24.projectselection.service;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_returnsUserDetailsWithStoredHashAndRoleAuthority() {
        User user = new User();
        user.setEmail("a@student.xjtlu.edu.cn");
        user.setPasswordHash("stored-hash");
        user.setRole(User.Role.student);
        user.setStatus(User.UserStatus.active);
        when(userRepository.findByEmail("a@student.xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("a@student.xjtlu.edu.cn");

        assertThat(details.getUsername()).isEqualTo("a@student.xjtlu.edu.cn");
        assertThat(details.getPassword()).isEqualTo("stored-hash");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("student");
    }

    @Test
    void loadUserByUsername_throwsWhenEmailNotFound() {
        when(userRepository.findByEmail("missing@xjtlu.edu.cn")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@xjtlu.edu.cn"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_disabledUser_returnsUserDetailsWithEnabledFalse() {
        User user = new User();
        user.setEmail("disabled@student.xjtlu.edu.cn");
        user.setPasswordHash("stored-hash");
        user.setRole(User.Role.student);
        user.setStatus(User.UserStatus.disabled);
        when(userRepository.findByEmail("disabled@student.xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("disabled@student.xjtlu.edu.cn");

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.getUsername()).isEqualTo("disabled@student.xjtlu.edu.cn");
    }

    // ── Security-focused tests ──────────────────────────────────────────

    @Test
    @DisplayName("Active user returns isEnabled=true")
    void loadUserByUsername_activeUser_isEnabled() {
        User user = new User();
        user.setEmail("active@student.xjtlu.edu.cn");
        user.setPasswordHash("stored-hash");
        user.setRole(User.Role.student);
        user.setStatus(User.UserStatus.active);
        when(userRepository.findByEmail("active@student.xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("active@student.xjtlu.edu.cn");

        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("Teacher user gets teacher authority")
    void loadUserByUsername_teacherUser_returnsTeacherAuthority() {
        User user = new User();
        user.setEmail("teacher@xjtlu.edu.cn");
        user.setPasswordHash("stored-hash");
        user.setRole(User.Role.teacher);
        user.setStatus(User.UserStatus.active);
        when(userRepository.findByEmail("teacher@xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("teacher@xjtlu.edu.cn");

        assertEquals(1, details.getAuthorities().size());
        assertEquals("teacher", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Admin user gets admin authority")
    void loadUserByUsername_adminUser_returnsAdminAuthority() {
        User user = new User();
        user.setEmail("admin@xjtlu.edu.cn");
        user.setPasswordHash("stored-hash");
        user.setRole(User.Role.admin);
        user.setStatus(User.UserStatus.active);
        when(userRepository.findByEmail("admin@xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("admin@xjtlu.edu.cn");

        assertEquals(1, details.getAuthorities().size());
        assertEquals("admin", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Disabled teacher user returns isEnabled=false")
    void loadUserByUsername_disabledTeacher_returnsEnabledFalse() {
        User user = new User();
        user.setEmail("disabled-teacher@xjtlu.edu.cn");
        user.setPasswordHash("stored-hash");
        user.setRole(User.Role.teacher);
        user.setStatus(User.UserStatus.disabled);
        when(userRepository.findByEmail("disabled-teacher@xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("disabled-teacher@xjtlu.edu.cn");

        assertFalse(details.isEnabled());
        assertEquals("teacher", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("Disabled admin user returns isEnabled=false")
    void loadUserByUsername_disabledAdmin_returnsEnabledFalse() {
        User user = new User();
        user.setEmail("disabled-admin@xjtlu.edu.cn");
        user.setPasswordHash("stored-hash");
        user.setRole(User.Role.admin);
        user.setStatus(User.UserStatus.disabled);
        when(userRepository.findByEmail("disabled-admin@xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("disabled-admin@xjtlu.edu.cn");

        assertFalse(details.isEnabled());
        assertEquals("admin", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("loadUserByUsername returns stored password hash, not plaintext")
    void loadUserByUsername_returnsStoredHashNotPlaintext() {
        User user = new User();
        user.setEmail("hashcheck@student.xjtlu.edu.cn");
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ12");
        user.setRole(User.Role.student);
        user.setStatus(User.UserStatus.active);
        when(userRepository.findByEmail("hashcheck@student.xjtlu.edu.cn")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("hashcheck@student.xjtlu.edu.cn");

        assertEquals("$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ12", details.getPassword());
    }

    @Test
    @DisplayName("UsernameNotFoundException message contains the email")
    void loadUserByUsername_notFound_exceptionContainsEmail() {
        when(userRepository.findByEmail("unknown@test.edu.cn")).thenReturn(Optional.empty());

        try {
            userDetailsService.loadUserByUsername("unknown@test.edu.cn");
        } catch (UsernameNotFoundException e) {
            assertThat(e.getMessage()).contains("unknown@test.edu.cn");
        }
    }
}

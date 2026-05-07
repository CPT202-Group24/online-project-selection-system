package com.group24.projectselection.service;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
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
}

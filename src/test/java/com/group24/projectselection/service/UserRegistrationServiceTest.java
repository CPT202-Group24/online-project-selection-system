package com.group24.projectselection.service;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService registrationService;

    @Test
    void register_encodesPasswordNormalizesEmailAndSavesUser() {
        when(passwordEncoder.encode("secret")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register(
                "  Alice  ",
                "  Name@STUDENT.XJTLU.EDU.CN  ",
                "secret",
                User.Role.student);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(passwordEncoder).encode("secret");

        User persisted = captor.getValue();
        assertThat(persisted.getEmail()).isEqualTo("name@student.xjtlu.edu.cn");
        assertThat(persisted.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(persisted.getName()).isEqualTo("Alice");
        assertThat(persisted.getRole()).isEqualTo(User.Role.student);
        assertThat(persisted.getStatus()).isEqualTo(User.UserStatus.active);
        assertThat(saved).isSameAs(persisted);
    }

    @Test
    void isValidRegistrationInput_rejectsWhenEmailDomainDoesNotMatchRole() {
        assertThat(registrationService.isValidRegistrationInput(
                "Bob", "bob@xjtlu.edu.cn", "p", User.Role.student)).isFalse();
    }

    @Test
    void isValidRegistrationInput_acceptsStudentAndTeacherUniversityEmails() {
        assertThat(registrationService.isValidRegistrationInput(
                "S", "s1@student.xjtlu.edu.cn", "p", User.Role.student)).isTrue();
        assertThat(registrationService.isValidRegistrationInput(
                "T", "t1@xjtlu.edu.cn", "p", User.Role.teacher)).isTrue();
    }
}

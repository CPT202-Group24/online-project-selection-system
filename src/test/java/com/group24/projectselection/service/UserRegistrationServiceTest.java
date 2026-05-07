package com.group24.projectselection.service;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("register encodes password, normalizes email and saves user")
    void register_encodesPasswordNormalizesEmailAndSavesUser() {
        when(passwordEncoder.encode("secret123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register(
                "  Alice  ",
                "  Name@STUDENT.XJTLU.EDU.CN  ",
                "secret123",
                User.Role.student);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(passwordEncoder).encode("secret123");

        User persisted = captor.getValue();
        assertEquals("name@student.xjtlu.edu.cn", persisted.getEmail());
        assertEquals("bcrypt-hash", persisted.getPasswordHash());
        assertEquals("Alice", persisted.getName());
        assertEquals(User.Role.student, persisted.getRole());
        assertEquals(User.UserStatus.active, persisted.getStatus());
        assertSame(persisted, saved);
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects when email domain does not match role")
    void isValidRegistrationInput_rejectsWhenEmailDomainDoesNotMatchRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Bob", "bob@xjtlu.edu.cn", "secret123", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput accepts student and teacher university emails")
    void isValidRegistrationInput_acceptsStudentAndTeacherUniversityEmails() {
        assertTrue(registrationService.isValidRegistrationInput(
                "S", "s1@student.xjtlu.edu.cn", "secret123", User.Role.student));
        assertTrue(registrationService.isValidRegistrationInput(
                "T", "t1@xjtlu.edu.cn", "secret123", User.Role.teacher));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects student email with teacher role")
    void isValidRegistrationInput_rejectsStudentEmailWithTeacherRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "T", "t1@student.xjtlu.edu.cn", "secret123", User.Role.teacher));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects teacher email with student role")
    void isValidRegistrationInput_rejectsTeacherEmailWithStudentRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "S", "s1@xjtlu.edu.cn", "secret123", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects password shorter than minimum length")
    void isValidRegistrationInput_rejectsShortPassword() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "abc", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput accepts password at minimum length")
    void isValidRegistrationInput_acceptsPasswordAtMinLength() {
        assertTrue(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "123456", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects admin role")
    void isValidRegistrationInput_rejectsAdminRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Admin", "admin@xjtlu.edu.cn", "secret123", User.Role.admin));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects blank name")
    void isValidRegistrationInput_rejectsBlankName() {
        assertFalse(registrationService.isValidRegistrationInput(
                "  ", "s@student.xjtlu.edu.cn", "secret123", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects blank password")
    void isValidRegistrationInput_rejectsBlankPassword() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "s@student.xjtlu.edu.cn", "  ", User.Role.student));
    }

    @Test
    @DisplayName("parseRegisterableRole returns null for admin")
    void parseRegisterableRole_returnsNullForAdmin() {
        assertNull(registrationService.parseRegisterableRole("admin"));
    }

    @Test
    @DisplayName("parseRegisterableRole returns null for invalid role string")
    void parseRegisterableRole_returnsNullForInvalidRole() {
        assertNull(registrationService.parseRegisterableRole("invalid"));
    }

    @Test
    @DisplayName("parseRegisterableRole returns student for valid student string")
    void parseRegisterableRole_returnsStudentForValidString() {
        assertEquals(User.Role.student, registrationService.parseRegisterableRole("student"));
    }

    @Test
    @DisplayName("parseRegisterableRole returns teacher for valid teacher string")
    void parseRegisterableRole_returnsTeacherForValidString() {
        assertEquals(User.Role.teacher, registrationService.parseRegisterableRole("teacher"));
    }
}

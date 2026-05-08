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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        when(passwordEncoder.encode("Secret123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register(
                "  Alice  ",
                "  Name@STUDENT.XJTLU.EDU.CN  ",
                "Secret123",
                User.Role.student);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(passwordEncoder).encode("Secret123");

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
                "Bob", "bob@xjtlu.edu.cn", "Secret123", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput accepts student and teacher university emails")
    void isValidRegistrationInput_acceptsStudentAndTeacherUniversityEmails() {
        assertTrue(registrationService.isValidRegistrationInput(
                "S", "s1@student.xjtlu.edu.cn", "Secret123", User.Role.student));
        assertTrue(registrationService.isValidRegistrationInput(
                "T", "t1@xjtlu.edu.cn", "Secret123", User.Role.teacher));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects student email with teacher role")
    void isValidRegistrationInput_rejectsStudentEmailWithTeacherRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "T", "t1@student.xjtlu.edu.cn", "Secret123", User.Role.teacher));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects teacher email with student role")
    void isValidRegistrationInput_rejectsTeacherEmailWithStudentRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "S", "s1@xjtlu.edu.cn", "Secret123", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects password shorter than minimum length")
    void isValidRegistrationInput_rejectsShortPassword() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "Abcdefg", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput accepts password at minimum length")
    void isValidRegistrationInput_acceptsPasswordAtMinLength() {
        assertTrue(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "Abcdef1x", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects admin role")
    void isValidRegistrationInput_rejectsAdminRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Admin", "admin@xjtlu.edu.cn", "Secret123", User.Role.admin));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects blank name")
    void isValidRegistrationInput_rejectsBlankName() {
        assertFalse(registrationService.isValidRegistrationInput(
                "  ", "s@student.xjtlu.edu.cn", "Secret123", User.Role.student));
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

    // ── Security-focused tests ──────────────────────────────────────────

    @Test
    @DisplayName("Password with special characters is accepted")
    void isValidRegistrationInput_acceptsPasswordWithSpecialCharacters() {
        assertTrue(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "P@$$w0rd!#%", User.Role.student));
    }

    @Test
    @DisplayName("Password with exactly MIN_LENGTH chars is accepted")
    void isValidRegistrationInput_acceptsPasswordWithExactMinLength() {
        assertTrue(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "Abcdef1x", User.Role.student));
    }

    @Test
    @DisplayName("Password with 5 chars (one below MIN_LENGTH) is rejected")
    void isValidRegistrationInput_rejectsPasswordOneBelowMinLength() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "Abcdefg", User.Role.student));
    }

    @Test
    @DisplayName("SQL injection attempt in email is safely rejected (no exception, returns false)")
    void isValidRegistrationInput_sqlInjectionInEmail_isSafelyRejected() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "'; DROP TABLE users; --@student.xjtlu.edu.cn", "Secret123", User.Role.student));
    }

    @Test
    @DisplayName("SQL injection attempt in email without valid domain is rejected")
    void isValidRegistrationInput_sqlInjectionInEmail_noDomain_isRejected() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "' OR 1=1 --", "Secret123", User.Role.student));
    }

    @Test
    @DisplayName("XSS attempt in email is safely rejected")
    void isValidRegistrationInput_xssInEmail_isSafelyRejected() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "<script>alert('xss')</script>@student.xjtlu.edu.cn", "Secret123", User.Role.student));
    }

    @Test
    @DisplayName("Email validation is case-insensitive for domain part")
    void isValidEmailForRole_caseInsensitiveDomain_matchesCorrectly() {
        assertTrue(registrationService.isValidEmailForRole("ALICE@STUDENT.XJTLU.EDU.CN", User.Role.student));
        assertTrue(registrationService.isValidEmailForRole("alice@student.xjtlu.edu.cn", User.Role.student));
        assertTrue(registrationService.isValidEmailForRole("TEACHER@XJTLU.EDU.CN", User.Role.teacher));
        assertTrue(registrationService.isValidEmailForRole("teacher@xjtlu.edu.cn", User.Role.teacher));
    }

    @Test
    @DisplayName("Student email with non-university domain is rejected")
    void isValidEmailForRole_studentWithGenericDomain_isRejected() {
        assertFalse(registrationService.isValidEmailForRole("alice@gmail.com", User.Role.student));
        assertFalse(registrationService.isValidEmailForRole("alice@hotmail.com", User.Role.student));
        assertFalse(registrationService.isValidEmailForRole("alice@xjtlu.com", User.Role.student));
    }

    @Test
    @DisplayName("Teacher email with non-university domain is rejected")
    void isValidEmailForRole_teacherWithGenericDomain_isRejected() {
        assertFalse(registrationService.isValidEmailForRole("teacher@gmail.com", User.Role.teacher));
        assertFalse(registrationService.isValidEmailForRole("teacher@yahoo.com", User.Role.teacher));
    }

    @Test
    @DisplayName("Staff email with student subdomain is rejected (prevents role escalation)")
    void isValidEmailForRole_staffEmailWithStudentSubdomain_isRejected() {
        assertFalse(registrationService.isValidEmailForRole("staff@student.xjtlu.edu.cn", User.Role.teacher));
    }

    @Test
    @DisplayName("register stores BCrypt hash, not plaintext password")
    void register_passwordIsBcryptEncodedNotPlaintext() {
        String rawPassword = "mySecret123";
        String bcryptHash = "$2a$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ12";
        when(passwordEncoder.encode(rawPassword)).thenReturn(bcryptHash);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register("Bob", "bob@student.xjtlu.edu.cn", rawPassword, User.Role.student);

        assertEquals(bcryptHash, saved.getPasswordHash());
        assertNotEquals(rawPassword, saved.getPasswordHash());
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("register trims whitespace from name")
    void register_trimsWhitespaceFromName() {
        when(passwordEncoder.encode("Secret123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register("  Bob Smith  ", "b@student.xjtlu.edu.cn", "Secret123", User.Role.student);

        assertEquals("Bob Smith", saved.getName());
    }

    @Test
    @DisplayName("register trims and lowercases email")
    void register_trimsAndLowercasesEmail() {
        when(passwordEncoder.encode("Secret123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register("Bob", "  BOB@STUDENT.XJTLU.EDU.CN  ", "Secret123", User.Role.student);

        assertEquals("bob@student.xjtlu.edu.cn", saved.getEmail());
    }

    @Test
    @DisplayName("register sets user status to active")
    void register_setsStatusToActive() {
        when(passwordEncoder.encode("Secret123")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = registrationService.register("Bob", "b@student.xjtlu.edu.cn", "Secret123", User.Role.student);

        assertEquals(User.UserStatus.active, saved.getStatus());
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects null email")
    void isValidRegistrationInput_rejectsNullEmail() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", null, "Secret123", User.Role.student));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects null role")
    void isValidRegistrationInput_rejectsNullRole() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "a@student.xjtlu.edu.cn", "Secret123", null));
    }

    @Test
    @DisplayName("isValidRegistrationInput rejects empty string email")
    void isValidRegistrationInput_rejectsEmptyEmail() {
        assertFalse(registrationService.isValidRegistrationInput(
                "Alice", "", "Secret123", User.Role.student));
    }
}

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

    // ── register tests ─────────────────────────────────────────────────

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

    // ── isValidPassword tests ──────────────────────────────────────────

    @Test
    @DisplayName("isValidPassword accepts password at minimum length with required character classes")
    void isValidPassword_acceptsPasswordAtMinLength() {
        assertTrue(registrationService.isValidPassword("Abcdef1x"));
    }

    @Test
    @DisplayName("isValidPassword rejects password shorter than minimum length")
    void isValidPassword_rejectsShortPassword() {
        assertFalse(registrationService.isValidPassword("Abcdefg"));
    }

    @Test
    @DisplayName("isValidPassword accepts password with special characters")
    void isValidPassword_acceptsPasswordWithSpecialCharacters() {
        assertTrue(registrationService.isValidPassword("P@$$w0rd!#%"));
    }

    @Test
    @DisplayName("isValidPassword rejects blank password")
    void isValidPassword_rejectsBlankPassword() {
        assertFalse(registrationService.isValidPassword("  "));
    }

    @Test
    @DisplayName("isValidPassword rejects null password")
    void isValidPassword_rejectsNullPassword() {
        assertFalse(registrationService.isValidPassword(null));
    }

    @Test
    @DisplayName("isValidPassword rejects password missing uppercase")
    void isValidPassword_rejectsPasswordMissingUppercase() {
        assertFalse(registrationService.isValidPassword("abcdef1x"));
    }

    @Test
    @DisplayName("isValidPassword rejects password missing lowercase")
    void isValidPassword_rejectsPasswordMissingLowercase() {
        assertFalse(registrationService.isValidPassword("ABCDEF1X"));
    }

    @Test
    @DisplayName("isValidPassword rejects password missing digit")
    void isValidPassword_rejectsPasswordMissingDigit() {
        assertFalse(registrationService.isValidPassword("Abcdefgh"));
    }

    // ── resolveRoleFromEmail tests ─────────────────────────────────────

    @Test
    @DisplayName("resolveRoleFromEmail returns student for student email")
    void resolveRoleFromEmail_studentEmail_returnsStudent() {
        assertSame(User.Role.student, registrationService.resolveRoleFromEmail("alice@student.xjtlu.edu.cn"));
    }

    @Test
    @DisplayName("resolveRoleFromEmail returns teacher for staff email")
    void resolveRoleFromEmail_staffEmail_returnsTeacher() {
        assertSame(User.Role.teacher, registrationService.resolveRoleFromEmail("bob@xjtlu.edu.cn"));
    }

    @Test
    @DisplayName("resolveRoleFromEmail returns null for non-university email")
    void resolveRoleFromEmail_nonUniversityEmail_returnsNull() {
        assertNull(registrationService.resolveRoleFromEmail("alice@gmail.com"));
    }

    @Test
    @DisplayName("resolveRoleFromEmail returns null for blank email")
    void resolveRoleFromEmail_blankEmail_returnsNull() {
        assertNull(registrationService.resolveRoleFromEmail("  "));
    }

    @Test
    @DisplayName("resolveRoleFromEmail is case-insensitive")
    void resolveRoleFromEmail_caseInsensitive() {
        assertSame(User.Role.student, registrationService.resolveRoleFromEmail("ALICE@STUDENT.XJTLU.EDU.CN"));
        assertSame(User.Role.teacher, registrationService.resolveRoleFromEmail("BOB@XJTLU.EDU.CN"));
    }

    // ── Security-focused tests ──────────────────────────────────────────

    @Test
    @DisplayName("SQL injection attempt in email is safely rejected by resolveRoleFromEmail")
    void resolveRoleFromEmail_sqlInjectionInEmail_returnsNull() {
        assertNull(registrationService.resolveRoleFromEmail(
                "'; DROP TABLE users; --@student.xjtlu.edu.cn"));
    }

    @Test
    @DisplayName("SQL injection attempt in email without valid domain returns null")
    void resolveRoleFromEmail_sqlInjectionNoDomain_returnsNull() {
        assertNull(registrationService.resolveRoleFromEmail("' OR 1=1 --"));
    }

    @Test
    @DisplayName("XSS attempt in email is safely rejected by resolveRoleFromEmail")
    void resolveRoleFromEmail_xssInEmail_returnsNull() {
        assertNull(registrationService.resolveRoleFromEmail(
                "<script>alert('xss')</script>@student.xjtlu.edu.cn"));
    }
}

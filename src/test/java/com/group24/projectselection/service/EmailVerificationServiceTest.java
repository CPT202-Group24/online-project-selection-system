package com.group24.projectselection.service;

import com.group24.projectselection.model.VerificationCode;
import com.group24.projectselection.repository.VerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private VerificationCodeRepository verificationCodeRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private static final String TEST_EMAIL = "test@student.xjtlu.edu.cn";

    @BeforeEach
    void setUp() {
    }

    // ── sendVerificationCode tests ─────────────────────────────────────

    @Test
    @DisplayName("sendVerificationCode: happy path generates and saves code, sends email")
    void sendVerificationCode_happyPath() {
        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_EMAIL))
                .thenReturn(Optional.empty());

        String result = emailVerificationService.sendVerificationCode(TEST_EMAIL);

        assertNull(result, "Should return null on success");

        ArgumentCaptor<VerificationCode> captor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(verificationCodeRepository).save(captor.capture());
        VerificationCode saved = captor.getValue();

        assertEquals(TEST_EMAIL, saved.getEmail());
        assertNotNull(saved.getCode());
        assertEquals(6, saved.getCode().length());
        assertTrue(saved.getCode().matches("\\d{6}"), "Code should be 6 digits");
        assertNotNull(saved.getExpiresAt());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()), "Expiry should be in the future");

        verify(emailService).sendVerificationCodeEmail(eq(TEST_EMAIL), eq(saved.getCode()));
    }

    @Test
    @DisplayName("sendVerificationCode: rate limit rejects if last code was created within 60 seconds")
    void sendVerificationCode_rateLimitRejects() {
        VerificationCode recentCode = new VerificationCode();
        recentCode.setEmail(TEST_EMAIL);
        recentCode.setCode("123456");
        recentCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        recentCode.setUsed(false);
        // Simulate createdAt being 30 seconds ago by using PrePersist simulation
        // Since createdAt is set via @PrePersist, we need to set it manually for test
        // The entity uses @PrePersist so createdAt is set on save, but for the mock we set it directly
        try {
            var field = VerificationCode.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(recentCode, LocalDateTime.now().minusSeconds(30));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_EMAIL))
                .thenReturn(Optional.of(recentCode));

        String result = emailVerificationService.sendVerificationCode(TEST_EMAIL);

        assertEquals("Please wait before requesting another code.", result);
        verify(verificationCodeRepository, never()).save(any());
        verify(emailService, never()).sendVerificationCodeEmail(any(), any());
    }

    @Test
    @DisplayName("sendVerificationCode: allows new code if last code was created more than 60 seconds ago")
    void sendVerificationCode_allowsAfterCooldown() {
        VerificationCode oldCode = new VerificationCode();
        oldCode.setEmail(TEST_EMAIL);
        oldCode.setCode("123456");
        oldCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        oldCode.setUsed(false);
        try {
            var field = VerificationCode.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(oldCode, LocalDateTime.now().minusSeconds(61));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_EMAIL))
                .thenReturn(Optional.of(oldCode));

        String result = emailVerificationService.sendVerificationCode(TEST_EMAIL);

        assertNull(result, "Should return null on success after cooldown");
        verify(verificationCodeRepository).save(any());
        verify(emailService).sendVerificationCodeEmail(eq(TEST_EMAIL), any());
    }

    // ── verifyCode tests ───────────────────────────────────────────────

    @Test
    @DisplayName("verifyCode: happy path matches code and marks as used")
    void verifyCode_happyPath() {
        VerificationCode vc = new VerificationCode();
        vc.setEmail(TEST_EMAIL);
        vc.setCode("654321");
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        vc.setUsed(false);

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_EMAIL))
                .thenReturn(Optional.of(vc));

        String result = emailVerificationService.verifyCode(TEST_EMAIL, "654321");

        assertNull(result, "Should return null when code matches");
        assertTrue(vc.getUsed(), "Code should be marked as used");
        verify(verificationCodeRepository).save(vc);
    }

    @Test
    @DisplayName("verifyCode: returns error when no code was sent")
    void verifyCode_noCodeSent() {
        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_EMAIL))
                .thenReturn(Optional.empty());

        String result = emailVerificationService.verifyCode(TEST_EMAIL, "123456");

        assertEquals("No verification code was sent to this email.", result);
    }

    @Test
    @DisplayName("verifyCode: returns error when code is expired")
    void verifyCode_expiredCode() {
        VerificationCode vc = new VerificationCode();
        vc.setEmail(TEST_EMAIL);
        vc.setCode("123456");
        vc.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        vc.setUsed(false);

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_EMAIL))
                .thenReturn(Optional.of(vc));

        String result = emailVerificationService.verifyCode(TEST_EMAIL, "123456");

        assertEquals("Verification code has expired. Please request a new one.", result);
        verify(verificationCodeRepository, never()).save(any());
    }

    @Test
    @DisplayName("verifyCode: returns error when code does not match")
    void verifyCode_wrongCode() {
        VerificationCode vc = new VerificationCode();
        vc.setEmail(TEST_EMAIL);
        vc.setCode("123456");
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        vc.setUsed(false);

        when(verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(TEST_EMAIL))
                .thenReturn(Optional.of(vc));

        String result = emailVerificationService.verifyCode(TEST_EMAIL, "999999");

        assertEquals("Incorrect verification code.", result);
        verify(verificationCodeRepository, never()).save(any());
    }
}

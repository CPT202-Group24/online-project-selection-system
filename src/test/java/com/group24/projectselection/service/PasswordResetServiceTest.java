package com.group24.projectselection.service;

import com.group24.projectselection.model.PasswordResetToken;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.PasswordResetTokenRepository;
import com.group24.projectselection.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("s@student.xjtlu.edu.cn");
        testUser.setPasswordHash("old-hash");
    }

    @AfterEach
    void tearDown() {
        testUser = null;
    }

    @Test
    @DisplayName("Create reset token for existing email - sends email with valid token")
    void createResetToken_emailExists_sendsEmailWithToken() {
        when(userRepository.findByEmail("s@student.xjtlu.edu.cn")).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        passwordResetService.createResetTokenAndSendEmail("s@student.xjtlu.edu.cn");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        PasswordResetToken saved = captor.getValue();
        assertEquals(testUser, saved.getUser());
        assertNotNull(saved.getToken());
        assertFalse(saved.getToken().isBlank());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
        assertFalse(saved.getUsed());

        verify(emailService).sendPasswordResetEmail(eq("s@student.xjtlu.edu.cn"), eq(saved.getToken()));
    }

    @Test
    @DisplayName("Validate valid token - updates password and marks token used")
    void validateAndReset_validToken_updatesPassword() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("abc");
        token.setUser(testUser);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPass")).thenReturn("new-hash");

        String error = passwordResetService.validateTokenAndResetPassword("abc", "newPass");

        assertNull(error);
        assertEquals("new-hash", testUser.getPasswordHash());
        assertTrue(token.getUsed());
        verify(userRepository).save(testUser);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("Validate invalid token - returns error and does not update password")
    void validateAndReset_invalidToken_returnsError() {
        when(tokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        String error = passwordResetService.validateTokenAndResetPassword("invalid", "newPass");

        assertEquals("Invalid reset link.", error);
        verify(userRepository, never()).save(any());
    }
}

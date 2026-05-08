package com.group24.projectselection.service;

import com.group24.projectselection.model.VerificationCode;
import com.group24.projectselection.repository.VerificationCodeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailVerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    public EmailVerificationService(VerificationCodeRepository verificationCodeRepository,
                                    EmailService emailService) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.emailService = emailService;
    }

    /**
     * Sends a 6-digit verification code to the given email.
     * @return null on success, or an error message string
     */
    public String sendVerificationCode(String email) {
        Optional<VerificationCode> latest =
                verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email);

        if (latest.isPresent()) {
            LocalDateTime createdAt = latest.get().getCreatedAt();
            if (createdAt != null && createdAt.plusSeconds(60).isAfter(LocalDateTime.now())) {
                return "Please wait before requesting another code.";
            }
        }

        String code = String.format("%06d", random.nextInt(1000000));

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verificationCodeRepository.save(verificationCode);

        emailService.sendVerificationCodeEmail(email, code);
        return null;
    }

    /**
     * Verifies a code submitted by the user.
     * @return null on success, or an error message string
     */
    public String verifyCode(String email, String code) {
        Optional<VerificationCode> latest =
                verificationCodeRepository.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email);

        if (latest.isEmpty()) {
            return "No verification code was sent to this email.";
        }

        VerificationCode vc = latest.get();

        if (vc.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "Verification code has expired. Please request a new one.";
        }

        if (!vc.getCode().equals(code)) {
            return "Incorrect verification code.";
        }

        vc.setUsed(true);
        verificationCodeRepository.save(vc);
        return null;
    }
}

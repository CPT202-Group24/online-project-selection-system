package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.service.EmailVerificationService;
import com.group24.projectselection.service.UserRegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class RegistrationController {

    private final UserRegistrationService registrationService;
    private final EmailVerificationService emailVerificationService;

    public RegistrationController(UserRegistrationService registrationService,
                                  EmailVerificationService emailVerificationService) {
        this.registrationService = registrationService;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/api/send-verification-code")
    @ResponseBody
    public Map<String, String> sendVerificationCode(@RequestParam String email) {
        if (!StringUtils.hasText(email)) {
            return Map.of("error", "Please enter your email.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (registrationService.emailExists(normalizedEmail)) {
            return Map.of("error", "An account with this email already exists.");
        }
        String error = emailVerificationService.sendVerificationCode(normalizedEmail);
        if (error != null) {
            return Map.of("error", error);
        }
        return Map.of("success", "Verification code sent.");
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String verificationCode,
            RedirectAttributes redirectAttributes) {

        if (!StringUtils.hasText(name)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter your name.");
            return "redirect:/register";
        }

        if (!StringUtils.hasText(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a password.");
            return "redirect:/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
            return "redirect:/register";
        }

        if (!registrationService.isValidPassword(password)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Password must be at least 8 characters and include uppercase, lowercase, and a digit.");
            return "redirect:/register";
        }

        String normalizedEmail = email.trim().toLowerCase();
        User.Role role = registrationService.resolveRoleFromEmail(normalizedEmail);
        if (role == null) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Please use your university email (@student.xjtlu.edu.cn or @xjtlu.edu.cn).");
            return "redirect:/register";
        }

        if (registrationService.emailExists(normalizedEmail)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "An account with this email already exists.");
            return "redirect:/register";
        }

        String verificationError = emailVerificationService.verifyCode(normalizedEmail, verificationCode);
        if (verificationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", verificationError);
            return "redirect:/register";
        }

        registrationService.register(name, normalizedEmail, password, role);
        return "redirect:/login?registered=true";
    }
}

package com.group24.projectselection.controller;

import com.group24.projectselection.service.PasswordResetService;
import com.group24.projectselection.service.UserRegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        passwordResetService.createResetTokenAndSendEmail(email);
        redirectAttributes.addFlashAttribute("infoMessage",
                "If an account exists with that email, a reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "reset-password";
        }

        if (password.length() < UserRegistrationService.MIN_PASSWORD_LENGTH) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Password must be at least " + UserRegistrationService.MIN_PASSWORD_LENGTH + " characters.");
            return "reset-password";
        }

        String error = passwordResetService.validateTokenAndResetPassword(token, password);
        if (error != null) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", error);
            return "reset-password";
        }

        return "redirect:/login?reset=true";
    }
}

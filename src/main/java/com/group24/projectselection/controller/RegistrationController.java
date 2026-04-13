package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.service.UserRegistrationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private static final String VALIDATION_ERROR =
            "Invalid email or missing required information.";

    private final UserRegistrationService registrationService;

    public RegistrationController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            RedirectAttributes redirectAttributes) {

        User.Role parsedRole = registrationService.parseRegisterableRole(role);
        if (!registrationService.isValidRegistrationInput(name, email, password, parsedRole)) {
            redirectAttributes.addFlashAttribute("errorMessage", VALIDATION_ERROR);
            return "redirect:/register";
        }

        if (registrationService.emailExists(email)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "An account with this email already exists.");
            return "redirect:/register";
        }

        registrationService.register(name, email, password, parsedRole);
        return "redirect:/login?registered=true";
    }
}

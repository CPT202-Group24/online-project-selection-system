package com.group24.projectselection.controller;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private static final String PROFILE_FORM = "profileForm";
    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication,
                              @RequestParam(defaultValue = "false") boolean success,
                              Model model) {
        User currentUser = findCurrentUser(authentication);
        if (!model.containsAttribute(PROFILE_FORM)) {
            model.addAttribute(PROFILE_FORM, ProfileForm.from(currentUser));
        }
        model.addAttribute("currentEmail", currentUser.getEmail());
        model.addAttribute("saveSuccess", success);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute(PROFILE_FORM) ProfileForm profileForm,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User currentUser = findCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentEmail", currentUser.getEmail());
            model.addAttribute("saveSuccess", false);
            return "profile";
        }

        currentUser.setName(profileForm.getName().trim());
        currentUser.setPhone(profileForm.getPhone().trim());
        currentUser.setDepartment(profileForm.getDepartment().trim());
        userRepository.save(currentUser);

        redirectAttributes.addAttribute("success", true);
        return "redirect:/profile";
    }

    private User findCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Current user not found: " + email));
    }

    public static class ProfileForm {
        @NotBlank(message = "Field is required")
        private String name;

        @NotBlank(message = "Field is required")
        private String phone;

        @NotBlank(message = "Field is required")
        private String department;

        public static ProfileForm from(User user) {
            ProfileForm form = new ProfileForm();
            form.setName(user.getName());
            form.setPhone(user.getPhone());
            form.setDepartment(user.getDepartment());
            return form;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }
}

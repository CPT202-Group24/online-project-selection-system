package com.group24.projectselection.service;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
public class UserRegistrationService {

    private static final Pattern UNIVERSITY_EMAIL =
            Pattern.compile("(?i)^[a-z0-9._%+-]+@xjtlu\\.edu\\.cn$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isValidUniversityEmail(String email) {
        return StringUtils.hasText(email) && UNIVERSITY_EMAIL.matcher(email.trim()).matches();
    }

    public boolean isValidRegistrationInput(String name, String email, String password, User.Role role) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(password)) {
            return false;
        }
        if (!isValidUniversityEmail(email)) {
            return false;
        }
        return role != null && role != User.Role.admin;
    }

    /**
     * @return null if role is not allowed for self-registration (e.g. admin)
     */
    public User.Role parseRegisterableRole(String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        try {
            User.Role r = User.Role.valueOf(role.trim().toLowerCase());
            if (r == User.Role.admin) {
                return null;
            }
            return r;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Transactional
    public User register(String name, String email, String password, User.Role role) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setName(name.trim());
        user.setRole(role);
        user.setStatus(User.UserStatus.active);
        return userRepository.save(user);
    }
}

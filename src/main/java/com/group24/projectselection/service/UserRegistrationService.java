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

    public static final int MIN_PASSWORD_LENGTH = 8;

    private static final Pattern PASSWORD_STRENGTH =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    /** Student accounts use the student subdomain. */
    private static final Pattern STUDENT_EMAIL =
            Pattern.compile("(?i)^[a-z0-9._%+-]+@student\\.xjtlu\\.edu\\.cn$");
    /** Staff (e.g. teacher) accounts use the main university domain (excluding student subdomain). */
    private static final Pattern STAFF_EMAIL =
            Pattern.compile("(?i)^[a-z0-9._%+-]+@(?!student\\.)xjtlu\\.edu\\.cn$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Resolves the role from the email domain.
     * @return student or teacher role, or null if the email does not match any valid university domain
     */
    public User.Role resolveRoleFromEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        String trimmed = email.trim();
        if (STUDENT_EMAIL.matcher(trimmed).matches()) {
            return User.Role.student;
        }
        if (STAFF_EMAIL.matcher(trimmed).matches()) {
            return User.Role.teacher;
        }
        return null;
    }

    /**
     * Validates password strength: at least 8 chars, one uppercase, one lowercase, one digit.
     */
    public boolean isValidPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }
        return PASSWORD_STRENGTH.matcher(password).matches();
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

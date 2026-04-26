package com.group24.projectselection.service;

import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserAdminService {

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserSummary> listAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(UserSummary::from)
                .toList();
    }

    @Transactional
    public UserSummary toggleStatus(Long userId, String currentAdminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new IllegalArgumentException("You cannot disable your own account.");
        }

        if (user.getStatus() == User.UserStatus.active) {
            user.setStatus(User.UserStatus.disabled);
        } else {
            user.setStatus(User.UserStatus.active);
        }

        return UserSummary.from(userRepository.save(user));
    }

    @Transactional
    public UserSummary updateRole(Long userId, String newRole, String currentAdminEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        User.Role parsedRole;
        try {
            parsedRole = User.Role.valueOf(newRole.trim().toLowerCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid role: " + newRole);
        }

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail) && parsedRole != User.Role.admin) {
            throw new IllegalArgumentException("You cannot change your own role away from admin.");
        }

        user.setRole(parsedRole);
        return UserSummary.from(userRepository.save(user));
    }

    public record UserSummary(
            Long id,
            String email,
            String name,
            String role,
            String status,
            String phone,
            String department,
            LocalDateTime createdAt
    ) {
        public static UserSummary from(User user) {
            return new UserSummary(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole().name(),
                    user.getStatus().name(),
                    user.getPhone(),
                    user.getDepartment(),
                    user.getCreatedAt()
            );
        }
    }
}

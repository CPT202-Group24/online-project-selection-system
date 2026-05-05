package com.group24.projectselection.controller;

import com.group24.projectselection.model.Notification;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.UserRepository;
import com.group24.projectselection.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/notifications")
    public String notificationsPage(Authentication authentication, Model model) {
        Long userId = getCurrentUser(authentication).getId();
        List<Notification> notifications = notificationService.listByUser(userId);
        model.addAttribute("notifications", notifications);
        return "notifications"; // 对应 notifications.html
    }

    @PostMapping("/notifications/{id}/read")
    public String markAsRead(@PathVariable("id") Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Notification marked as read.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/notifications";
    }

    @GetMapping("/api/notifications/unread-count")
    @ResponseBody
    public Map<String, Long> unreadCount(Authentication authentication) {
        Long userId = getCurrentUser(authentication).getId();
        long unreadCount = notificationService.countUnread(userId);
        return Map.of("unreadCount", unreadCount);
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public List<NotificationResponse> listNotifications(Authentication authentication) {
        Long userId = getCurrentUser(authentication).getId();
        return notificationService.listByUser(userId).stream()
                .map(n -> new NotificationResponse(n.getId(), n.getMessage(), n.getIsRead(), n.getCreatedAt()))
                .toList();
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Current user not found: " + email));
    }

    public record NotificationResponse(Long id, String message, Boolean isRead, LocalDateTime createdAt) {
    }
}

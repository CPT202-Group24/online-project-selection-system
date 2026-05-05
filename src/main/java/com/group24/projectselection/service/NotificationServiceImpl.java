package com.group24.projectselection.service;

import com.group24.projectselection.model.Notification;
import com.group24.projectselection.model.User;
import com.group24.projectselection.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    public List<Notification> listByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, String message) {
        Notification notification = new Notification();

        User user = new User();
        user.setId(userId);
        notification.setUser(user);

        notification.setMessage(message);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }
}
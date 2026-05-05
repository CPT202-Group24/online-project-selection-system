package com.group24.projectselection.service;

import com.group24.projectselection.model.Notification;

import java.util.List;

public interface NotificationService {

    long countUnread(Long userId);

    List<Notification> listByUser(Long userId);

    void markAsRead(Long notificationId);

    Notification createNotification(Long userId, String message);
}
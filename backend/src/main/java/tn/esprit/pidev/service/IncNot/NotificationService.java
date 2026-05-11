package tn.esprit.pidev.service.IncNot;

import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification saveNotification(Notification notification);
    void deleteNotification(Long id);
    Notification getNotificationById(Long id);
    List<Notification> getAllNotifications();
    void sendInternalNotificationsToAgents(IncidentNotificationDTO dto);
    void sendExternalNotificationsToAgents(IncidentNotificationDTO dto);
    void sendDelayNotificationsToPassengers(IncidentNotificationDTO dto);
    List<Notification> getNotificationsForUser(String username);
    Notification markAsRead(Long notificationId);
}
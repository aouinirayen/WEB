package tn.esprit.pidev.controller.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.entity.Notification;
import tn.esprit.pidev.service.IncNot.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/add")
    public Notification createNotification(@RequestBody Notification notification) {
        return notificationService.saveNotification(notification);
    }

    @PutMapping("/update/{id}")
    public Notification updateNotification(@PathVariable Long id, @RequestBody Notification notification) {
        notification.setId(id);
        return notificationService.saveNotification(notification);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }

    @GetMapping("/get/{id}")
    public Notification getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id);
    }

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    // ✅ NEW — get MY notifications (logged in user)
    @GetMapping("/my")
    public List<Notification> getMyNotifications(Authentication authentication) {
        String username = authentication.getName();
        return notificationService.getNotificationsForUser(username);
    }

    // ✅ NEW — mark notification as read
    @PatchMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }
}
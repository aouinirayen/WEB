package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.NotificationRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class GlobalNotificationService {

    private static final Logger logger = Logger.getLogger(GlobalNotificationService.class.getName());
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public GlobalNotificationService(NotificationRepository notificationRepository,
                                      UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notify(Long userId, String title, String message) {
        userRepository.findById(userId).ifPresent(user -> {
            Notification n = new Notification(title, message, NotifStatusEnum.SENT, user, LocalDateTime.now());
            notificationRepository.save(n);
            logger.info("Notification -> " + user.getUsername() + ": " + title);
        });
    }

    public void notify(User user, String title, String message) {
        Notification n = new Notification(title, message, NotifStatusEnum.SENT, user, LocalDateTime.now());
        notificationRepository.save(n);
    }

    public void notifyAllByRole(RoleEnum role, String title, String message) {
        List<User> users = userRepository.findByRole(role);
        for (User user : users) {
            notificationRepository.save(new Notification(title, message, NotifStatusEnum.SENT, user, LocalDateTime.now()));
        }
    }

    public void notifyAllPassengers(String title, String message) {
        notifyAllByRole(RoleEnum.PASSENGER, title, message);
    }

    public void notifyAllAgents(String title, String message) {
        notifyAllByRole(RoleEnum.AGENT, title, message);
    }

    public void notifyAllAdmins(String title, String message) {
        notifyAllByRole(RoleEnum.ADMIN, title, message);
    }
}
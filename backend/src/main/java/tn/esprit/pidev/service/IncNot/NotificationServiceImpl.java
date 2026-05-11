package tn.esprit.pidev.service.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.NotificationRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationServiceImpl.class.getName());

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailingService mailingService;

    @Override
    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // ✅ INTERNAL — save notification in DB for each AGENT
    @Override
    public void sendInternalNotificationsToAgents(IncidentNotificationDTO dto) {
        List<User> agents = userRepository.findByRole(RoleEnum.AGENT);
        for (User agent : agents) {
            Notification notification = new Notification(
                    "🚨 New Incident: " + dto.getTitle(),
                    "Location: " + dto.getLocation() +
                            " | Severity: " + dto.getSeverity() +
                            " | Reported by: " + dto.getReportedByName(),
                    NotifStatusEnum.SENT,
                    agent,                   // ✅ User object not String
                    LocalDateTime.now()
            );
            notificationRepository.save(notification);
            logger.info("Internal notification saved for agent: " + agent.getUsername());
        }
    }

    // ✅ EXTERNAL — send email to each AGENT
    @Override
    public void sendExternalNotificationsToAgents(IncidentNotificationDTO dto) {
        List<User> agents = userRepository.findByRole(RoleEnum.AGENT);
        for (User agent : agents) {
            mailingService.sendIncidentNotificationEmail(
                    agent.getEmail(),
                    agent.getName(),
                    dto
            );
        }
    }

    // ✅ INTERNAL + EXTERNAL — notify each PASSENGER
    @Override
    public void sendDelayNotificationsToPassengers(IncidentNotificationDTO dto) {
        List<User> passengers = userRepository.findByRole(RoleEnum.PASSENGER);
        for (User passenger : passengers) {
            Notification notification = new Notification(
                    "⚠️ Transport Delay",
                    "Delay at: " + dto.getLocation() +
                            " | Severity: " + dto.getSeverity(),
                    NotifStatusEnum.SENT,
                    passenger,               // ✅ User object not String
                    LocalDateTime.now()
            );
            notificationRepository.save(notification);
            logger.info("Internal delay notification saved for passenger: " + passenger.getUsername());

            mailingService.sendTransportDelayEmail(
                    passenger.getEmail(),
                    passenger.getName(),
                    dto
            );
        }
    }

    // ✅ Get notifications by User object (not String)
    @Override
    public List<Notification> getNotificationsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return notificationRepository.findByUser(user);
    }

    // ✅ Mark as READ
    @Override
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setStatus(NotifStatusEnum.READ);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }
}
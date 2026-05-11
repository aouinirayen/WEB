package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pidev.entity.Notification;
import tn.esprit.pidev.entity.NotifStatusEnum;
import tn.esprit.pidev.entity.User;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser(User user);                                    // ✅ par User
    List<Notification> findByUserAndStatus(User user, NotifStatusEnum status);   // ✅ par User + status
}
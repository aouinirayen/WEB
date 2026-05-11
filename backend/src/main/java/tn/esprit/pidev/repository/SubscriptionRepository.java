package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Subscription;
import tn.esprit.pidev.entity.SubscriptionStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByPassengerId(Long passengerId);
    List<Subscription> findByStatut(SubscriptionStatus statut);
    // Retourne une List pour eviter IncorrectResultSizeDataAccessException
    List<Subscription> findByPassengerIdAndStatut(Long passengerId, SubscriptionStatus statut);
    List<Subscription> findByPricingPlanId(Long planId);
    List<Subscription> findByPricingPlanCreatedById(Long operatorId);

    List<Subscription> findByStatutAndDateFin(SubscriptionStatus statut, LocalDate dateFin);

    List<Subscription> findByStatutAndDateFinBefore(SubscriptionStatus statut, LocalDate date);

    List<Subscription> findByStatutAndDateFinAndReminderEmail7dSentFalse(SubscriptionStatus statut, LocalDate dateFin);

    List<Subscription> findByStatutAndDateFinAndReminderEmail1dSentFalse(SubscriptionStatus statut, LocalDate dateFin);
}

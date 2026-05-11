package tn.esprit.pidev.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.*;
import tn.esprit.pidev.service.EmailService;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoRenewalScheduler {

    private final SubscriptionRepository   subscriptionRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final EmailService             emailService;

    @Scheduled(cron = "${subscription.jobs.cron:0 0 8 * * ?}")
    public void processRenewalsAndNotifications() {
        log.info("=== Auto-renewal scheduler started ===");
        expireOldSubscriptions();           // ← AJOUTER cette ligne
        processRenewalsAndNotifications(LocalDate.now());
        log.info("=== Auto-renewal scheduler finished ===");
    }

    // ── Expire subscriptions whose end date has passed ──
    private void expireOldSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> toExpire = subscriptionRepo
                .findByStatutAndDateFinBefore(SubscriptionStatus.ACTIVE, today);
        if (toExpire.isEmpty()) return;
        log.info("Expiring {} subscription(s) past their end date", toExpire.size());
        toExpire.forEach(s -> s.setStatut(SubscriptionStatus.EXPIRED));
        subscriptionRepo.saveAll(toExpire);
    }

    // Called by the manual test endpoint
    public void processRenewalsAndNotifications(LocalDate referenceDate) {
        log.info("=== Scheduler run for reference date {} ===", referenceDate);

        for (int daysLeft : new int[]{7, 1}) {
            LocalDate targetDate = referenceDate.plusDays(daysLeft);

            // ── FIX : use findByStatutAndDateFin (correct method name in repo) ──
            List<Subscription> expiring = subscriptionRepo
                    .findByStatutAndDateFin(SubscriptionStatus.ACTIVE, targetDate);

            log.info("Subscriptions expiring in {} day(s) [{}]: {}", daysLeft, targetDate, expiring.size());

            for (Subscription sub : expiring) {
                handleExpiring(sub, daysLeft);
            }
        }

        log.info("=== Scheduler run finished ===");
    }

    private void handleExpiring(Subscription sub, int daysLeft) {
        User passenger = sub.getPassenger();
        PricingPlan plan = sub.getPricingPlan();

        String name    = passenger.getName() != null ? passenger.getName() : passenger.getUsername();
        String planNom = plan != null ? plan.getNom() : "Subscription";

        // Always send reminder regardless of auto-renewal status
        emailService.sendRenewalReminderEmail(
                passenger.getEmail(),
                name,
                planNom,
                sub.getDateFin(),
                daysLeft,
                sub.isAutoRenewal()
        );

        // Renew only on J-1 if auto-renewal is enabled
        if (daysLeft == 1 && sub.isAutoRenewal()) {
            renewSubscription(sub);
        }
    }

    private void renewSubscription(Subscription sub) {
        PricingPlan plan = sub.getPricingPlan();
        if (plan == null) {
            log.warn("Cannot renew subscription {} — no pricing plan attached", sub.getId());
            return;
        }

        User passenger = sub.getPassenger();
        log.info("Renewing subscription {} for passenger {}", sub.getId(), passenger.getUsername());

        // Create new subscription starting day after current one ends
        Subscription renewed = new Subscription();
        renewed.setPassenger(passenger);
        renewed.setPricingPlan(plan);
        renewed.setDateDebut(sub.getDateFin().plusDays(1));
        renewed.setDateFin(sub.getDateFin().plusDays(plan.getDureeEnJours()));
        renewed.setStatut(SubscriptionStatus.ACTIVE);
        renewed.setAutoRenewal(true);
        subscriptionRepo.save(renewed);

        // Award loyalty points
        int points = (int) Math.floor(plan.getPrix());
        loyaltyRepo.findByPassengerId(passenger.getId()).ifPresent(acc -> {
            acc.setPointsCumules(acc.getPointsCumules() + points);
            loyaltyRepo.save(acc);
        });

        // Send confirmation email
        String name = passenger.getName() != null ? passenger.getName() : passenger.getUsername();
        emailService.sendRenewalConfirmationEmail(
                passenger.getEmail(), name, plan.getNom(), renewed.getDateFin()
        );

        log.info("Subscription {} renewed → new sub ends {}", sub.getId(), renewed.getDateFin());
    }
}
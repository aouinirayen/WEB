package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.service.GlobalNotificationService;
import tn.esprit.pidev.dto.SubscriptionRequest;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements ISubscriptionService {

    private final SubscriptionRepository   subscriptionRepo;
    private final PricingPlanRepository    planRepo;
    private final UserRepository           userRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final PointTransactionRepository txRepo;
    private final ReductionRepository      reductionRepo;
    private final EmailService             emailService;
    private final GlobalNotificationService globalNotif;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepo,
                                   PricingPlanRepository planRepo,
                                   UserRepository userRepo,
                                   LoyaltyAccountRepository loyaltyRepo,
                                   PointTransactionRepository txRepo,
                                   ReductionRepository reductionRepo,
                                   EmailService emailService,
                                   GlobalNotificationService globalNotif) {
        this.globalNotif = globalNotif;
        this.subscriptionRepo = subscriptionRepo;
        this.planRepo         = planRepo;
        this.userRepo         = userRepo;
        this.loyaltyRepo      = loyaltyRepo;
        this.txRepo           = txRepo;
        this.reductionRepo    = reductionRepo;
        this.emailService     = emailService;
    }

    // ─── Subscribe ───────────────────────────────────────────────────────────

    @Override
    public SubscriptionResponse subscribe(SubscriptionRequest request, Long passengerId) {
        User passenger = getPassenger(passengerId);

        PricingPlan plan = planRepo.findById(request.getPricingPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPricingPlanId()));

        double prixFinal = plan.getPrix();
        if (request.getCodeReduction() != null && !request.getCodeReduction().isBlank()) {
            Reduction red = reductionRepo.findByCode(request.getCodeReduction().toUpperCase())
                    .orElseThrow(() -> new RuntimeException("Invalid discount code."));
            if (red.getDateExpiration().isBefore(LocalDate.now())) {
                throw new RuntimeException("This discount code has expired.");
            }
            LoyaltyAccount la = loyaltyRepo.findByPassengerId(passengerId).orElse(null);
            int pts = la != null ? la.getPointsCumules() : 0;
            if (pts < red.getPointsRequis()) {
                throw new RuntimeException("Insufficient points for this code. Required: "
                        + red.getPointsRequis() + ", you have: " + pts);
            }
            prixFinal = plan.getPrix() - (plan.getPrix() * red.getPourcentage() / 100);
        }

        LocalDate debut = LocalDate.now();
        LocalDate fin   = debut.plusDays(plan.getDureeEnJours());
        Subscription sub = new Subscription(debut, fin, SubscriptionStatus.ACTIVE, passenger, plan);
        Subscription saved = subscriptionRepo.save(sub);

        // Generate loyalty points
        int pointsGagnes = (int) Math.floor(prixFinal);
        LoyaltyAccount account = loyaltyRepo.findByPassengerId(passengerId)
                .orElseGet(() -> loyaltyRepo.save(new LoyaltyAccount(passenger)));
        account.setPointsCumules(account.getPointsCumules() + pointsGagnes);
        account.setNiveau(calculerTier(account.getPointsCumules()));
        loyaltyRepo.save(account);

        txRepo.save(new PointTransaction(
                pointsGagnes, TransactionType.EARNED, LocalDateTime.now(),
                "Points earned — subscription: " + plan.getNom(), account));

        return SubscriptionResponse.fromEntity(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Override
    public SubscriptionResponse getById(Long id) {
        return SubscriptionResponse.fromEntity(findById(id));
    }

    @Override
    public List<SubscriptionResponse> getAll() {
        List<Subscription> all = subscriptionRepo.findAll();
        expireIfNeeded(all);
        return all.stream().map(SubscriptionResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getByOperator(Long operatorId) {
        List<Subscription> subs = subscriptionRepo.findByPricingPlanCreatedById(operatorId);
        expireIfNeeded(subs);
        return subs.stream().map(SubscriptionResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getByPassenger(Long passengerId) {
        List<Subscription> subs = subscriptionRepo.findByPassengerId(passengerId);
        expireIfNeeded(subs);
        return subs.stream().map(SubscriptionResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionResponse> getByStatut(SubscriptionStatus statut) {
        // Expire first so the filter returns accurate results
        expireIfNeeded(subscriptionRepo.findAll());
        return subscriptionRepo.findByStatut(statut).stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ─── Cancel ───────────────────────────────────────────────────────────────

    @Override
    public SubscriptionResponse cancel(Long id, Long passengerId) {
        Subscription sub = findById(id);
        if (!sub.getPassenger().getId().equals(passengerId)) {
            throw new RuntimeException("You can only cancel your own subscriptions.");
        }
        sub.setStatut(SubscriptionStatus.CANCELLED);
        
        globalNotif.notify(passengerId, "Abonnement annule",
            "Votre abonnement a ete annule avec succes.");

        return SubscriptionResponse.fromEntity(subscriptionRepo.save(sub));
    }

    // ─── Auto-renewal toggle ──────────────────────────────────────────────────

    @Override
    public SubscriptionResponse updateAutoRenewal(Long id, Long passengerId, boolean autoRenewal) {
        Subscription sub = findById(id);

        if (!sub.getPassenger().getId().equals(passengerId)) {
            throw new RuntimeException("You can only modify your own subscriptions.");
        }
        if (sub.getStatut() != SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Only active subscriptions can update auto-renewal.");
        }

        sub.setAutoRenewal(autoRenewal);
        Subscription saved = subscriptionRepo.save(sub);

        User   passenger = saved.getPassenger();
        String name      = passenger.getName() != null ? passenger.getName() : passenger.getUsername();
        String planName  = saved.getPricingPlan() != null ? saved.getPricingPlan().getNom() : "Subscription";

        if (autoRenewal) {
            emailService.sendAutoRenewalEnabledEmail(
                    passenger.getEmail(), name, planName, saved.getDateFin());
        } else {
            emailService.sendAutoRenewalDisabledEmail(
                    passenger.getEmail(), name, planName, saved.getDateFin());
        }

        return SubscriptionResponse.fromEntity(saved);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        findById(id);
        subscriptionRepo.deleteById(id);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Expires on-the-fly any ACTIVE subscription whose end date is in the past.
     * This ensures the UI is always accurate without waiting for the nightly cron.
     */
    private void expireIfNeeded(List<Subscription> subs) {
        LocalDate today = LocalDate.now();
        List<Subscription> toExpire = subs.stream()
                .filter(s -> s.getStatut() == SubscriptionStatus.ACTIVE
                        && s.getDateFin().isBefore(today))
                .collect(Collectors.toList());
        if (!toExpire.isEmpty()) {
            toExpire.forEach(s -> s.setStatut(SubscriptionStatus.EXPIRED));
            subscriptionRepo.saveAll(toExpire);
        }
    }

    private Subscription findById(Long id) {
        return subscriptionRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + id));
    }

    private User getPassenger(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (user.getRole() != RoleEnum.PASSENGER) {
            throw new RuntimeException("Access denied: only PASSENGER accounts can subscribe.");
        }
        return user;
    }

    private LoyaltyTier calculerTier(int pts) {
        if (pts >= 500) return LoyaltyTier.GOLD;
        if (pts >= 200) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }
}
package tn.esprit.pidev.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pidev.dto.PaymentInitRequest;
import tn.esprit.pidev.dto.PaymentInitResponse;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class StripePaymentServiceImpl implements IStripePaymentService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.checkout.currency:tnd}")
    private String checkoutCurrency;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    private final PendingPaymentRepository pendingRepo;
    private final PricingPlanRepository planRepo;
    private final ReductionRepository reductionRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final UserRepository userRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final PointTransactionRepository txRepo;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    public StripePaymentServiceImpl(PendingPaymentRepository pendingRepo,
                                    PricingPlanRepository planRepo,
                                    ReductionRepository reductionRepo,
                                    SubscriptionRepository subscriptionRepo,
                                    UserRepository userRepo,
                                    LoyaltyAccountRepository loyaltyRepo,
                                    PointTransactionRepository txRepo,
                                    EmailService emailService) {
        this.pendingRepo = pendingRepo;
        this.planRepo = planRepo;
        this.reductionRepo = reductionRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.userRepo = userRepo;
        this.loyaltyRepo = loyaltyRepo;
        this.txRepo = txRepo;
        this.emailService = emailService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Override
    public PaymentInitResponse initiatePayment(PaymentInitRequest request) {
        String currency = checkoutCurrency != null ? checkoutCurrency.trim().toLowerCase() : "tnd";

        User passenger = userRepo.findById(request.getPassengerId())
                .orElseThrow(() -> new RuntimeException(
                        "Passager introuvable (passengerId=" + request.getPassengerId() + "). Reconnectez-vous."));
        PricingPlan plan = planRepo.findById(request.getPricingPlanId())
                .orElseThrow(() -> new RuntimeException("Plan introuvable (pricingPlanId=" + request.getPricingPlanId() + ")."));

        double montant = plan.getPrix() != null ? plan.getPrix() : 0d;
        if (request.getCodeReduction() != null
                && !request.getCodeReduction().isBlank()) {
            Reduction red = reductionRepo
                    .findByCode(request.getCodeReduction().toUpperCase().trim())
                    .orElseThrow(() -> new RuntimeException("Code de réduction inconnu ou invalide."));
            montant = montant - (montant * red.getPourcentage() / 100);
        }

        String paymentMode = request.getPaymentMode() != null
                ? request.getPaymentMode().trim().toUpperCase()
                : "CASH";
        if ("POINTS".equals(paymentMode)) {
            return initiatePointsPayment(request, passenger, plan, montant);
        }

        if (montant <= 0) {
            throw new RuntimeException(
                    "Montant final invalide (≤ 0). Vérifiez le prix du plan ou le pourcentage de réduction.");
        }

        long unitAmount = toStripeSmallestUnit(montant, currency);
        validateStripeMinimum(unitAmount, currency, montant);

        try {
            SessionCreateParams params = buildCheckoutSessionParams(plan, currency, unitAmount, request, passenger);
            Session session;
            try {
                session = Session.create(params);
            } catch (Exception stripeEx) {
                if ("tnd".equals(currency)) {
                    String fallbackCurrency = "eur";
                    long fallbackUnitAmount = toStripeSmallestUnit(montant, fallbackCurrency);
                    validateStripeMinimum(fallbackUnitAmount, fallbackCurrency, montant);
                    SessionCreateParams fallbackParams = buildCheckoutSessionParams(plan, fallbackCurrency, fallbackUnitAmount, request, passenger);
                    session = Session.create(fallbackParams);
                } else {
                    throw stripeEx;
                }
            }

            PendingPayment pending = new PendingPayment();
            pending.setStripeSessionId(session.getId());
            pending.setPassengerId(request.getPassengerId());
            pending.setPricingPlanId(request.getPricingPlanId());
            pending.setCodeReduction(request.getCodeReduction());
            pending.setMontantDT(montant);
            pending.setAutoRenewal(Boolean.TRUE.equals(request.getAutoRenewal()));
            pendingRepo.save(pending);

            PaymentInitResponse response = new PaymentInitResponse();
            response.setCheckoutUrl(session.getUrl());
            response.setSessionId(session.getId());
            response.setMontantDT(montant);
            response.setPlanNom(plan.getNom());
            return response;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur Stripe : " + e.getMessage(), e);
        }
    }

    @Transactional
    protected PaymentInitResponse initiatePointsPayment(
            PaymentInitRequest request,
            User passenger,
            PricingPlan plan,
            double montantFinalDT
    ) {
        if (montantFinalDT <= 0) {
            throw new RuntimeException("Montant final invalide pour un paiement en points.");
        }

        int requiredPoints = (int) Math.ceil(montantFinalDT * 10d); // 10 points = 1 DT
        if (request.getPointsToUse() != null) {
            if (request.getPointsToUse() <= 0) {
                throw new RuntimeException("pointsToUse doit être strictement positif.");
            }
            if (!request.getPointsToUse().equals(requiredPoints)) {
                throw new RuntimeException("pointsToUse incohérent avec le montant final.");
            }
        }

        LoyaltyAccount account = loyaltyRepo
                .findByPassengerId(passenger.getId())
                .orElseGet(() -> loyaltyRepo.save(new LoyaltyAccount(passenger)));
        int currentPoints = account.getPointsCumules() != null ? account.getPointsCumules() : 0;
        if (currentPoints < requiredPoints) {
            throw new RuntimeException("Points insuffisants. Requis : " + requiredPoints + ", disponible : " + currentPoints + ".");
        }

        LocalDate debut = LocalDate.now();
        LocalDate fin = debut.plusDays(plan.getDureeEnJours());
        Subscription sub = new Subscription(debut, fin, SubscriptionStatus.ACTIVE, passenger, plan);
        sub.setAutoRenewal(Boolean.TRUE.equals(request.getAutoRenewal()));
        subscriptionRepo.save(sub);

        account.setPointsCumules(currentPoints - requiredPoints);
        account.setNiveau(calculerTier(account.getPointsCumules()));
        loyaltyRepo.save(account);

        txRepo.save(new PointTransaction(
                requiredPoints,
                TransactionType.REDEEMED,
                LocalDateTime.now(),
                "Paiement en points — " + plan.getNom(),
                account));

        if (sub.isAutoRenewal() && passenger.getEmail() != null && !passenger.getEmail().isBlank()) {
            String plansUrl = frontendUrl != null ? frontendUrl.replaceAll("/$", "") + "/passenger/plans" : "";
            emailService.sendAutoRenewalActivatedEmail(
                    passenger.getEmail(),
                    passenger.getName() != null ? passenger.getName() : passenger.getUsername(),
                    plan.getNom(),
                    fin);
        }

        PaymentInitResponse response = new PaymentInitResponse();
        response.setCheckoutUrl(null);
        response.setSessionId("POINTS-" + sub.getId());
        response.setMontantDT(montantFinalDT);
        response.setPlanNom(plan.getNom());
        return response;
    }

    private SessionCreateParams buildCheckoutSessionParams(
            PricingPlan plan,
            String currency,
            long unitAmount,
            PaymentInitRequest request,
            User passenger) {

        SessionCreateParams.LineItem.PriceData.ProductData.Builder productBuilder =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Abonnement : " + plan.getNom());
        if (plan.getDescription() != null && !plan.getDescription().trim().isEmpty()) {
            productBuilder.setDescription(plan.getDescription().trim());
        }

        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .setCustomerEmail(passenger.getEmail())
                .setCustomerCreation(SessionCreateParams.CustomerCreation.ALWAYS)
                .putMetadata("passengerId", String.valueOf(request.getPassengerId()))
                .putMetadata("pricingPlanId", String.valueOf(request.getPricingPlanId()))
                .putMetadata("autoRenewal", Boolean.TRUE.equals(request.getAutoRenewal()) ? "true" : "false")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(unitAmount)
                                                .setProductData(productBuilder.build())
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    @Override
    @Transactional
    public SubscriptionResponse confirmPayment(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return completeCheckoutSession(session);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur confirmation Stripe : " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void processStripeWebhook(String payload, String sigHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new RuntimeException("stripe.webhook.secret non configuré (voir application.properties).");
        }
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret.trim());
            if (!"checkout.session.completed".equals(event.getType())) {
                return;
            }
            StripeObject obj = event.getDataObjectDeserializer().getObject().orElse(null);
            if (!(obj instanceof Session)) {
                return;
            }
            Session session = (Session) obj;
            completeCheckoutSession(session);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Signature webhook Stripe invalide.", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Webhook Stripe : " + e.getMessage(), e);
        }
    }

    /**
     * Crée l'abonnement une seule fois (idempotent : succès GET + webhook Stripe).
     */
    @Transactional
    public SubscriptionResponse completeCheckoutSession(Session session) throws Exception {
        String sessionId = session.getId();
        if (!"complete".equals(session.getStatus())
                && !"paid".equals(session.getPaymentStatus())) {
            throw new RuntimeException("Paiement non confirmé par Stripe");
        }

        PendingPayment pending = pendingRepo
                .findByStripeSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session introuvable : " + sessionId));

        if (pending.getStatus() == PendingPayment.PaymentStatus.SUCCESS) {
            if (pending.getSubscriptionId() != null) {
                Subscription existing = subscriptionRepo.findById(pending.getSubscriptionId())
                        .orElseThrow(() -> new RuntimeException("Abonnement introuvable pour cette session."));
                return SubscriptionResponse.fromEntity(existing);
            }
            throw new RuntimeException("Ce paiement a déjà été traité.");
        }

        User passenger = userRepo.findById(pending.getPassengerId())
                .orElseThrow(() -> new RuntimeException("Passenger introuvable"));
        PricingPlan plan = planRepo.findById(pending.getPricingPlanId())
                .orElseThrow(() -> new RuntimeException("Plan introuvable"));

        LocalDate debut = LocalDate.now();
        LocalDate fin = debut.plusDays(plan.getDureeEnJours());
        Subscription sub = new Subscription(
                debut, fin, SubscriptionStatus.ACTIVE, passenger, plan);
        sub.setAutoRenewal(pending.isAutoRenewal());
        String customerId = session.getCustomer();
        if (customerId != null && !customerId.isBlank()) {
            sub.setStripeCustomerId(customerId);
        }
        subscriptionRepo.save(sub);

        int points = (int) Math.floor(pending.getMontantDT());
        LoyaltyAccount account = loyaltyRepo
                .findByPassengerId(pending.getPassengerId())
                .orElseGet(() -> loyaltyRepo.save(new LoyaltyAccount(passenger)));
        account.setPointsCumules(account.getPointsCumules() + points);
        account.setNiveau(calculerTier(account.getPointsCumules()));
        loyaltyRepo.save(account);

        txRepo.save(new PointTransaction(
                points, TransactionType.EARNED,
                LocalDateTime.now(),
                "Paiement Stripe — " + plan.getNom(),
                account));

        pending.setStatus(PendingPayment.PaymentStatus.SUCCESS);
        pending.setSubscriptionId(sub.getId());
        pendingRepo.save(pending);

        if (sub.isAutoRenewal() && passenger.getEmail() != null && !passenger.getEmail().isBlank()) {
            String plansUrl = frontendUrl != null ? frontendUrl.replaceAll("/$", "") + "/passenger/plans" : "";
            emailService.sendAutoRenewalActivatedEmail(
                    passenger.getEmail(),
                    passenger.getName() != null ? passenger.getName() : passenger.getUsername(),
                    plan.getNom(),
                    fin);
        }

        return SubscriptionResponse.fromEntity(sub);
    }

    private static long toStripeSmallestUnit(double montantMajor, String currency) {
        if ("tnd".equals(currency)) {
            return Math.round(montantMajor * 1000d);
        }
        return Math.round(montantMajor * 100d);
    }

    private static void validateStripeMinimum(long unitAmount, String currency, double montantMajor) {
        long min = "tnd".equals(currency) ? 1000L : 50L;
        if (unitAmount < min) {
            throw new RuntimeException(String.format(
                    "Montant trop faible pour Stripe (minimum environ %s selon la devise). Montant calculé : %.3f.",
                    currency.toUpperCase(),
                    montantMajor));
        }
    }

    private LoyaltyTier calculerTier(int pts) {
        if (pts >= 500) return LoyaltyTier.GOLD;
        if (pts >= 200) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }

}

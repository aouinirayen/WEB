package tn.esprit.pidev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pidev.dto.*;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLServiceImpl implements IMLService {

    private final SubscriptionRepository   subscriptionRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final UserRepository           userRepo;
    private final RestTemplate             restTemplate;
    private final ReductionRepository      reductionRepo;   // ← NEW
    private final EmailService             emailService;    // ← NEW

    @Value("${ml.api.url:http://localhost:5000}")
    private String mlApiUrl;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private User getUser(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private MLApiResponse callFlask(Long passengerId) {
        MLApiRequest req = buildRequest(passengerId);
        try {
            return restTemplate.postForObject(
                    mlApiUrl + "/predict/all", req, MLApiResponse.class);
        } catch (Exception e) {
            log.error("Flask API unreachable — falling back to heuristic: {}", e.getMessage());
            return fallbackHeuristic(passengerId);
        }
    }

    private MLApiRequest buildRequest(Long passengerId) {
        List<Subscription> subs = subscriptionRepo.findByPassengerId(passengerId);
        LoyaltyAccount loyalty  = loyaltyRepo.findByPassengerId(passengerId).orElse(null);

        int nbAbs   = subs.size();
        int nbAnn   = (int) subs.stream()
                .filter(s -> s.getStatut() == SubscriptionStatus.CANCELLED).count();
        int nbRenew = nbAbs - nbAnn;

        Subscription active = subs.stream()
                .filter(s -> s.getStatut() == SubscriptionStatus.ACTIVE)
                .findFirst().orElse(null);

        int    joursRestants = 0;
        int    aActif        = 0;
        String planType      = "FREE";
        String transportType = "BUS";

        if (active != null) {
            joursRestants = (int) ChronoUnit.DAYS.between(LocalDate.now(), active.getDateFin());
            joursRestants = Math.max(joursRestants, 0);
            aActif        = 1;

            PricingPlan plan = active.getPricingPlan();
            if (plan != null) {
                if (plan.getType() != null) {
                    planType = plan.getType().name();
                }
                try {
                    Object transport = plan.getClass()
                            .getMethod("getTransportType")
                            .invoke(plan);
                    if (transport != null) {
                        transportType = transport.toString();
                    }
                } catch (NoSuchMethodException ignored) {
                    log.debug("PricingPlan.getTransportType() not found — using default BUS");
                } catch (Exception ex) {
                    log.warn("Error reading transportType: {}", ex.getMessage());
                }
            }
        }

        double prixMoyen = subs.stream()
                .filter(s -> s.getPricingPlan() != null)
                .mapToDouble(s -> s.getPricingPlan().getPrix())
                .average().orElse(0.0);

        int dureeMoy = (int) subs.stream()
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getDateDebut(), s.getDateFin()))
                .filter(d -> d > 0)
                .average().orElse(90.0);

        boolean aExpire     = subs.stream().anyMatch(s -> s.getStatut() == SubscriptionStatus.EXPIRED);
        int expireSansRenew = (aExpire && aActif == 0) ? 1 : 0;

        long maxJours  = subs.stream()
                .mapToLong(s -> ChronoUnit.DAYS.between(s.getDateDebut(), LocalDate.now()))
                .max().orElse(30L);
        int anciennete = (int) Math.max(maxJours, 30L);

        int    points = loyalty != null ? loyalty.getPointsCumules() : 0;
        String tier   = loyalty != null ? loyalty.getNiveau().name() : "BRONZE";

        MLApiRequest req = new MLApiRequest();
        req.setNbAbonnements(nbAbs);
        req.setNbAnnulations(nbAnn);
        req.setNbRenouvellements(nbRenew);
        req.setAncienneteJours(anciennete);
        req.setTransportType(transportType);
        req.setPlanType(planType);
        req.setPrixMoyen(prixMoyen);
        req.setDureeMoyJours(dureeMoy);
        req.setMontantTotalPaye(prixMoyen * nbRenew);
        req.setPointsCumules(points);
        req.setNiveauLoyalty(tier);
        req.setNbTransactions(0);
        req.setAAbonnementActif(aActif);
        req.setJoursRestants(joursRestants);
        req.setExpireSansRenew(expireSansRenew);
        return req;
    }

    // ─── Public endpoints ────────────────────────────────────────────────────

    @Override
    public PlanRecommendationResponse recommendPlan(Long passengerId) {
        User user = getUser(passengerId);
        MLApiResponse resp = callFlask(passengerId);

        PlanRecommendationResponse dto = new PlanRecommendationResponse();
        dto.setUserId(passengerId);
        dto.setUsername(user.getUsername());
        dto.setRecommendedPlan(resp.getRecommendation().getPlan());
        dto.setConfidence(resp.getRecommendation().getConfidence());
        dto.setReason(buildRecommendReason(resp));
        return dto;
    }

    @Override
    public ChurnPredictionResponse predictChurn(Long passengerId) {
        User user = getUser(passengerId);
        MLApiResponse resp = callFlask(passengerId);

        ChurnPredictionResponse dto = new ChurnPredictionResponse();
        dto.setUserId(passengerId);
        dto.setUsername(user.getUsername());
        dto.setChurnProbability(resp.getChurn().getProbability());
        dto.setRiskLevel(resp.getChurn().getRiskLevel());
        dto.setSuggestedAction(buildActionMessage(resp.getAction()));
        dto.setSuggestedPromoCode(
                "HIGH".equals(resp.getChurn().getRiskLevel())     ? "FLASH15"  :
                        "MODERATE".equals(resp.getChurn().getRiskLevel()) ? "SUMMER20" : null
        );
        return dto;
    }

    @Override
    public CLVResponse predictCLV(Long passengerId) {
        User user = getUser(passengerId);
        MLApiResponse resp = callFlask(passengerId);

        CLVResponse dto = new CLVResponse();
        dto.setPassengerId(passengerId);
        dto.setUsername(user.getUsername());
        dto.setClvValue(resp.getClv().getValue());
        dto.setCurrency("DT");
        dto.setAction(resp.getAction());
        dto.setInterpretation(buildCLVInterpretation(resp.getClv().getValue()));
        return dto;
    }

    @Override
    public List<ChurnPredictionResponse> predictChurnAll() {
        return userRepo.findAll().stream()
                .filter(u -> u.getRole() == RoleEnum.PASSENGER)
                .map(u -> predictChurn(u.getId()))
                .collect(Collectors.toList());
    }

    // ─── NEW : Send Action + generate promo + send email ─────────────────────

    @Override
    public ActionSendResponse sendAction(Long passengerId) {
        User user = getUser(passengerId);
        MLApiResponse resp = callFlask(passengerId);

        String riskLevel = resp.getChurn().getRiskLevel();
        String action    = resp.getAction();

        // Discount % based on risk level
        double discount = switch (riskLevel) {
            case "HIGH"     -> 20.0;
            case "MODERATE" -> 10.0;
            default         ->  5.0;
        };

        // Generate unique promo code : ML-{USERNAME_UP_TO_6}-{4_RANDOM_CHARS}
        String userPart = user.getUsername().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (userPart.length() > 6) userPart = userPart.substring(0, 6);

        String promoCode = "ML-" + userPart + "-" + generateRandomCode(4);
        // Ensure uniqueness — regenerate on collision (extremely rare)
        while (reductionRepo.findByCode(promoCode).isPresent()) {
            promoCode = "ML-" + userPart + "-" + generateRandomCode(4);
        }

        // Save in reduction table
        // pointsRequis = 0 (gift), createdBy = null (ML-generated, no specific operator)
        Reduction reduction = new Reduction(
                promoCode,
                discount,
                LocalDate.now().plusDays(7),
                0,
                null
        );
        reductionRepo.save(reduction);
        log.info("ML promo code {} ({}%) created for passenger {}", promoCode, discount, user.getUsername());

        // Send email to passenger
        boolean emailSent = false;
        LocalDate expiry  = LocalDate.now().plusDays(7);
        try {
            emailService.sendActionPromoEmail(
                    user.getEmail(),
                    user.getName() != null ? user.getName() : user.getUsername(),
                    promoCode,
                    discount,
                    buildActionMessage(action),
                    expiry
            );
            emailSent = true;
            log.info("Action promo email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send action email to {}: {}", user.getEmail(), e.getMessage());
        }

        // Build response
        ActionSendResponse dto = new ActionSendResponse();
        dto.setPassengerId(passengerId);
        dto.setUsername(user.getUsername());
        dto.setAction(action);
        dto.setPromoCode(promoCode);
        dto.setDiscountPercentage(discount);
        dto.setRiskLevel(riskLevel);
        dto.setMessage(buildActionMessage(action));
        dto.setEmailSent(emailSent);
        return dto;
    }

    // ─── Message helpers ─────────────────────────────────────────────────────

    private String buildRecommendReason(MLApiResponse resp) {
        return switch (resp.getRecommendation().getPlan()) {
            case "PREMIUM" -> "Loyal profile with high engagement — Premium plan recommended.";
            case "BASIC"   -> "Intermediate profile — the Basic plan suits your habits.";
            default        -> "New user — start with the free plan.";
        };
    }

    private String buildActionMessage(String action) {
        if (action == null) return "Keep engagement with loyalty benefits.";
        return switch (action) {
            case "OFFRE_RETENTION_PREMIUM"  -> "Send a premium retention offer immediately.";
            case "CODE_PROMO_URGENCE"       -> "High-risk client — send code FLASH15 urgently.";
            case "PROPOSER_UPGRADE_PREMIUM" -> "Eligible for premium — suggest an upgrade.";
            case "PROPOSER_UPGRADE_BASIC"   -> "Upgrade to Basic plan recommended.";
            case "BOOST_POINTS_LOYALTY"     -> "Activate a points bonus to re-engage.";
            default                         -> "Keep engagement with loyalty benefits.";
        };
    }

    private String buildCLVInterpretation(double clv) {
        if (clv >= 300) return "High-value client — maximum retention priority.";
        if (clv >= 150) return "Medium-value client — good upsell potential.";
        if (clv >= 50)  return "Developing client — encourage engagement.";
        return "Low-value client — offer an attractive entry-level plan.";
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb  = new StringBuilder();
        Random        rnd = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ─── Fallback heuristic (when Flask is down) ─────────────────────────────

    private MLApiResponse fallbackHeuristic(Long passengerId) {
        List<Subscription> subs = subscriptionRepo.findByPassengerId(passengerId);
        int pts = loyaltyRepo.findByPassengerId(passengerId)
                .map(LoyaltyAccount::getPointsCumules).orElse(0);

        double f1    = Math.min(subs.size() / 10.0, 1.0);
        double f2    = Math.min(subs.stream().filter(s -> s.getPricingPlan() != null)
                .mapToDouble(s -> s.getPricingPlan().getPrix()).average().orElse(0) / 50.0, 1.0);
        double f3    = Math.min(pts / 600.0, 1.0);
        long   ann   = subs.stream().filter(s -> s.getStatut() == SubscriptionStatus.CANCELLED).count();
        double f4    = 1.0 - Math.min(ann / 5.0, 1.0);
        double score = f1*0.30 + f2*0.35 + f3*0.25 + f4*0.10;

        MLApiResponse resp = new MLApiResponse();

        MLApiResponse.ChurnResult churn = new MLApiResponse.ChurnResult();
        churn.setProbability(0.5); churn.setLabel(0); churn.setRiskLevel("UNKNOWN");
        resp.setChurn(churn);

        MLApiResponse.ClvResult clv = new MLApiResponse.ClvResult();
        clv.setValue(0.0); clv.setCurrency("DT");
        resp.setClv(clv);

        MLApiResponse.RecommendResult rec = new MLApiResponse.RecommendResult();
        rec.setPlan(score >= 0.65 ? "PREMIUM" : score >= 0.35 ? "BASIC" : "FREE");
        rec.setConfidence(score * 100);
        resp.setRecommendation(rec);

        resp.setAction("MAINTENIR_ENGAGEMENT");
        return resp;
    }
}
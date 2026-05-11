package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pending_payment")
public class PendingPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String stripeSessionId;

    @Column(nullable = false)
    private Long passengerId;

    @Column(nullable = false)
    private Long pricingPlanId;

    private String codeReduction;

    @Column(nullable = false)
    private Double montantDT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /** Souhait de renouvellement automatique (propagé à l'abonnement créé). */
    @Column(nullable = false)
    private boolean autoRenewal = false;

    /** Abonnement créé après paiement (idempotence webhook / success). */
    private Long subscriptionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public enum PaymentStatus { PENDING, SUCCESS, FAILED }

    public PendingPayment() {
        this.createdAt = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public Long getId() { return id; }
    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String s) { this.stripeSessionId = s; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Long getPricingPlanId() { return pricingPlanId; }
    public void setPricingPlanId(Long pricingPlanId) { this.pricingPlanId = pricingPlanId; }
    public String getCodeReduction() { return codeReduction; }
    public void setCodeReduction(String codeReduction) { this.codeReduction = codeReduction; }
    public Double getMontantDT() { return montantDT; }
    public void setMontantDT(Double montantDT) { this.montantDT = montantDT; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(boolean autoRenewal) { this.autoRenewal = autoRenewal; }
    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }
}
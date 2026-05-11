package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "subscription")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus statut;

    // Lien avec User (Passenger qui a souscrit)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    // Lien avec le plan souscrit
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pricing_plan_id", nullable = false)
    private PricingPlan pricingPlan;

    /** Renouvellement souhaité (rappels + lien de paiement avant échéance ; extension manuelle / futur prélèvement). */
    @Column(name = "auto_renewal", nullable = false)
    private boolean autoRenewal = false;

    /** Client Stripe (Checkout avec customer_creation) pour renouvellements futurs. */
    @Column(name = "stripe_customer_id", length = 64)
    private String stripeCustomerId;

    @Column(name = "reminder_email_7d_sent", nullable = false)
    private boolean reminderEmail7dSent = false;

    @Column(name = "reminder_email_1d_sent", nullable = false)
    private boolean reminderEmail1dSent = false;

    // Constructors
    public Subscription() {}

    public Subscription(LocalDate dateDebut, LocalDate dateFin, SubscriptionStatus statut,
                        User passenger, PricingPlan pricingPlan) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.passenger = passenger;
        this.pricingPlan = pricingPlan;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public SubscriptionStatus getStatut() { return statut; }
    public void setStatut(SubscriptionStatus statut) { this.statut = statut; }
    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }
    public PricingPlan getPricingPlan() { return pricingPlan; }
    public void setPricingPlan(PricingPlan pricingPlan) { this.pricingPlan = pricingPlan; }

    public boolean isAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(boolean autoRenewal) { this.autoRenewal = autoRenewal; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String stripeCustomerId) { this.stripeCustomerId = stripeCustomerId; }
    public boolean isReminderEmail7dSent() { return reminderEmail7dSent; }
    public void setReminderEmail7dSent(boolean reminderEmail7dSent) { this.reminderEmail7dSent = reminderEmail7dSent; }
    public boolean isReminderEmail1dSent() { return reminderEmail1dSent; }
    public void setReminderEmail1dSent(boolean reminderEmail1dSent) { this.reminderEmail1dSent = reminderEmail1dSent; }
}

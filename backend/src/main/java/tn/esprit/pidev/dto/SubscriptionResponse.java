package tn.esprit.pidev.dto;

import tn.esprit.pidev.entity.Subscription;
import tn.esprit.pidev.entity.SubscriptionStatus;

import java.time.LocalDate;

public class SubscriptionResponse {

    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private SubscriptionStatus statut;
    private Long passengerId;
    private String passengerUsername;
    private String passengerName;
    private PricingPlanResponse pricingPlan;
    private Integer pointsGagnes;
    private Boolean autoRenewal;
    private String stripeCustomerId;

    public SubscriptionResponse() {}

    public static SubscriptionResponse fromEntity(Subscription s) {
        SubscriptionResponse dto = new SubscriptionResponse();
        dto.setId(s.getId());
        dto.setDateDebut(s.getDateDebut());
        dto.setDateFin(s.getDateFin());
        dto.setStatut(s.getStatut());
        if (s.getPassenger() != null) {
            dto.setPassengerId(s.getPassenger().getId());
            dto.setPassengerUsername(s.getPassenger().getUsername());
            dto.setPassengerName(s.getPassenger().getName());
        }
        if (s.getPricingPlan() != null) {
            dto.setPricingPlan(PricingPlanResponse.fromEntity(s.getPricingPlan()));
            Double prix = s.getPricingPlan().getPrix();
            dto.setPointsGagnes(prix != null ? (int) Math.floor(prix) : 0);
        }
        dto.setAutoRenewal(s.isAutoRenewal());
        dto.setStripeCustomerId(s.getStripeCustomerId());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public SubscriptionStatus getStatut() { return statut; }
    public void setStatut(SubscriptionStatus statut) { this.statut = statut; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public String getPassengerUsername() { return passengerUsername; }
    public void setPassengerUsername(String passengerUsername) { this.passengerUsername = passengerUsername; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public PricingPlanResponse getPricingPlan() { return pricingPlan; }
    public void setPricingPlan(PricingPlanResponse pricingPlan) { this.pricingPlan = pricingPlan; }
    public Integer getPointsGagnes() { return pointsGagnes; }
    public void setPointsGagnes(Integer pointsGagnes) { this.pointsGagnes = pointsGagnes; }
    public Boolean getAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(Boolean autoRenewal) { this.autoRenewal = autoRenewal; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public void setStripeCustomerId(String stripeCustomerId) { this.stripeCustomerId = stripeCustomerId; }
}

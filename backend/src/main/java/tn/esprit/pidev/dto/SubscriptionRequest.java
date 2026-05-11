package tn.esprit.pidev.dto;

// ===== REQUEST DTO (Passenger → souscrire à un plan) =====
public class SubscriptionRequest {

    private Long pricingPlanId;
    private String codeReduction; // optionnel

    public SubscriptionRequest() {}

    public Long getPricingPlanId() { return pricingPlanId; }
    public void setPricingPlanId(Long pricingPlanId) { this.pricingPlanId = pricingPlanId; }
    public String getCodeReduction() { return codeReduction; }
    public void setCodeReduction(String codeReduction) { this.codeReduction = codeReduction; }
}

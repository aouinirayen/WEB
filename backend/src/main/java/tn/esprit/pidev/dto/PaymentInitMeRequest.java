package tn.esprit.pidev.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentInitMeRequest {
    @NotNull(message = "pricingPlanId est requis")
    private Long pricingPlanId;
    private String codeReduction;
    private Boolean autoRenewal;
    /** CASH (Stripe) ou POINTS (wallet fidélité). */
    private String paymentMode;
    /** Nombre de points à débiter si paymentMode=POINTS. */
    private Integer pointsToUse;

    public PaymentInitMeRequest() {}

    public Long getPricingPlanId() { return pricingPlanId; }
    public void setPricingPlanId(Long pricingPlanId) { this.pricingPlanId = pricingPlanId; }
    public String getCodeReduction() { return codeReduction; }
    public void setCodeReduction(String codeReduction) { this.codeReduction = codeReduction; }
    public Boolean getAutoRenewal() { return autoRenewal; }
    public void setAutoRenewal(Boolean autoRenewal) { this.autoRenewal = autoRenewal; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public Integer getPointsToUse() { return pointsToUse; }
    public void setPointsToUse(Integer pointsToUse) { this.pointsToUse = pointsToUse; }
}

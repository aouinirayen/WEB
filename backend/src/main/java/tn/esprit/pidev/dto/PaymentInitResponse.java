package tn.esprit.pidev.dto;

public class PaymentInitResponse {
    private String checkoutUrl;   // URL Stripe → rediriger le passenger
    private String sessionId;     // ID session Stripe
    private Double montantDT;
    private String planNom;

    public PaymentInitResponse() {}
    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public Double getMontantDT() { return montantDT; }
    public void setMontantDT(Double montantDT) { this.montantDT = montantDT; }
    public String getPlanNom() { return planNom; }
    public void setPlanNom(String planNom) { this.planNom = planNom; }
}
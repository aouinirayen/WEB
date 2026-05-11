package tn.esprit.pidev.dto;

public class ChurnPredictionResponse {
    private Long userId;
    private String username;
    private double churnProbability;
    private String riskLevel;
    private String suggestedAction;
    private String suggestedPromoCode;

    public ChurnPredictionResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public double getChurnProbability() { return churnProbability; }
    public void setChurnProbability(double churnProbability) { this.churnProbability = churnProbability; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
    public String getSuggestedPromoCode() { return suggestedPromoCode; }
    public void setSuggestedPromoCode(String suggestedPromoCode) { this.suggestedPromoCode = suggestedPromoCode; }
}

package tn.esprit.pidev.dto;

public class PlanRecommendationResponse {
    private Long userId;
    private String username;
    private String recommendedPlan;
    private double confidence;
    private String reason;

    public PlanRecommendationResponse() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRecommendedPlan() { return recommendedPlan; }
    public void setRecommendedPlan(String recommendedPlan) { this.recommendedPlan = recommendedPlan; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

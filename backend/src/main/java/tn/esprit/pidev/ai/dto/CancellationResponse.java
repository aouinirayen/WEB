package tn.esprit.pidev.ai.dto;

public class CancellationResponse {

    private double probability;
    private String riskLevel;
    private String message;

    public CancellationResponse() {}

    public CancellationResponse(double probability, String riskLevel, String message) {
        this.probability = probability;
        this.riskLevel = riskLevel;
        this.message = message;
    }

    public double getProbability() { return probability; }
    public void setProbability(double probability) { this.probability = probability; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

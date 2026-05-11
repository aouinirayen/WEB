package tn.esprit.pidev.dto;

import java.util.List;

public class PartPredictionDTO {

    private String partName;
    private String referenceCode;
    private String category;

    private int currentStock;
    private int predictedQuantityNeeded;

    private String predictedDateNeeded;   // ISO date string "YYYY-MM-DD"
    private int daysUntilNeeded;          // negative = already overdue

    private double confidenceScore;       // 0.0 - 1.0
    private String method;                // "FREQUENCY_ANALYSIS" or "LINEAR_REGRESSION"
    private Urgency urgency;              // URGENT / SOON / MONITOR

    private String explanation;           // human-readable reason
    private List<String> affectedVehicles;

    // Per-vehicle prediction (used in vehicle-scoped results)
    private Long vehicleId;
    private String vehiclePlate;

    public enum Urgency { URGENT, SOON, MONITOR }

    // ── Getters / Setters ─────────────────────────────────────────
    public String getPartName()                        { return partName; }
    public void setPartName(String v)                  { this.partName = v; }
    public String getReferenceCode()                   { return referenceCode; }
    public void setReferenceCode(String v)             { this.referenceCode = v; }
    public String getCategory()                        { return category; }
    public void setCategory(String v)                  { this.category = v; }
    public int getCurrentStock()                       { return currentStock; }
    public void setCurrentStock(int v)                 { this.currentStock = v; }
    public int getPredictedQuantityNeeded()            { return predictedQuantityNeeded; }
    public void setPredictedQuantityNeeded(int v)      { this.predictedQuantityNeeded = v; }
    public String getPredictedDateNeeded()             { return predictedDateNeeded; }
    public void setPredictedDateNeeded(String v)       { this.predictedDateNeeded = v; }
    public int getDaysUntilNeeded()                    { return daysUntilNeeded; }
    public void setDaysUntilNeeded(int v)              { this.daysUntilNeeded = v; }
    public double getConfidenceScore()                 { return confidenceScore; }
    public void setConfidenceScore(double v)           { this.confidenceScore = v; }
    public String getMethod()                          { return method; }
    public void setMethod(String v)                    { this.method = v; }
    public Urgency getUrgency()                        { return urgency; }
    public void setUrgency(Urgency v)                  { this.urgency = v; }
    public String getExplanation()                     { return explanation; }
    public void setExplanation(String v)               { this.explanation = v; }
    public List<String> getAffectedVehicles()          { return affectedVehicles; }
    public void setAffectedVehicles(List<String> v)    { this.affectedVehicles = v; }
    public Long getVehicleId()                         { return vehicleId; }
    public void setVehicleId(Long v)                   { this.vehicleId = v; }
    public String getVehiclePlate()                    { return vehiclePlate; }
    public void setVehiclePlate(String v)              { this.vehiclePlate = v; }
}

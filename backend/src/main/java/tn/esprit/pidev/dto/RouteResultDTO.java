package tn.esprit.pidev.dto;

import java.util.List;

public class RouteResultDTO {

    private Long   lineId;
    private String lineCode;
    private String method;           // "DIJKSTRA" or "WEIGHTED_AI"

    private List<StopNode> optimalRoute;
    private List<StopNode> originalRoute;

    private double totalDistanceKm;
    private double estimatedFuelLiters;
    private double estimatedDurationMin;
    private double efficiencyScore;     // 0-100
    private String explanation;

    // ── Traffic simulation ────────────────────────────────────────
    private String trafficLevel;        // LOW / MEDIUM / HIGH
    private double trafficDelayMin;

    public static class StopNode {
        private Long   id;
        private String name;
        private int    sequence;
        private double latitude;
        private double longitude;
        private double distanceFromPrevKm;
        private double weightScore;      // AI weight (lower = better)
        private String roadType;         // HIGHWAY / URBAN / RURAL

        public StopNode(Long id, String name, int sequence,
                        double lat, double lon,
                        double distFromPrev, double weight, String roadType) {
            this.id = id; this.name = name; this.sequence = sequence;
            this.latitude = lat; this.longitude = lon;
            this.distanceFromPrevKm = distFromPrev;
            this.weightScore = weight; this.roadType = roadType;
        }

        public Long getId()                   { return id; }
        public String getName()               { return name; }
        public int getSequence()              { return sequence; }
        public double getLatitude()           { return latitude; }
        public double getLongitude()          { return longitude; }
        public double getDistanceFromPrevKm() { return distanceFromPrevKm; }
        public double getWeightScore()        { return weightScore; }
        public String getRoadType()           { return roadType; }
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public Long   getLineId()                        { return lineId; }
    public void   setLineId(Long v)                  { this.lineId = v; }
    public String getLineCode()                      { return lineCode; }
    public void   setLineCode(String v)              { this.lineCode = v; }
    public String getMethod()                        { return method; }
    public void   setMethod(String v)                { this.method = v; }
    public List<StopNode> getOptimalRoute()          { return optimalRoute; }
    public void   setOptimalRoute(List<StopNode> v)  { this.optimalRoute = v; }
    public List<StopNode> getOriginalRoute()         { return originalRoute; }
    public void   setOriginalRoute(List<StopNode> v) { this.originalRoute = v; }
    public double getTotalDistanceKm()               { return totalDistanceKm; }
    public void   setTotalDistanceKm(double v)       { this.totalDistanceKm = v; }
    public double getEstimatedFuelLiters()           { return estimatedFuelLiters; }
    public void   setEstimatedFuelLiters(double v)   { this.estimatedFuelLiters = v; }
    public double getEstimatedDurationMin()          { return estimatedDurationMin; }
    public void   setEstimatedDurationMin(double v)  { this.estimatedDurationMin = v; }
    public double getEfficiencyScore()               { return efficiencyScore; }
    public void   setEfficiencyScore(double v)       { this.efficiencyScore = v; }
    public String getExplanation()                   { return explanation; }
    public void   setExplanation(String v)           { this.explanation = v; }
    public String getTrafficLevel()                  { return trafficLevel; }
    public void   setTrafficLevel(String v)          { this.trafficLevel = v; }
    public double getTrafficDelayMin()               { return trafficDelayMin; }
    public void   setTrafficDelayMin(double v)       { this.trafficDelayMin = v; }
}

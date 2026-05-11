package tn.esprit.pidev.ai.dto;

public class SatisfactionResponse {

    private double predictedScore;
    private int stars;
    private String message;
    private String driverName;
    private String route;
    private double driverAvgScore;
    private int driverTotalRatings;

    public SatisfactionResponse() {}

    public SatisfactionResponse(double predictedScore, int stars, String message) {
        this.predictedScore = predictedScore;
        this.stars = stars;
        this.message = message;
    }

    public double getPredictedScore() { return predictedScore; }
    public void setPredictedScore(double predictedScore) { this.predictedScore = predictedScore; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public double getDriverAvgScore() { return driverAvgScore; }
    public void setDriverAvgScore(double driverAvgScore) { this.driverAvgScore = driverAvgScore; }

    public int getDriverTotalRatings() { return driverTotalRatings; }
    public void setDriverTotalRatings(int driverTotalRatings) { this.driverTotalRatings = driverTotalRatings; }
}

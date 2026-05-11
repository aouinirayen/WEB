package tn.esprit.pidev.ai.dto;

import java.time.LocalDate;

public class MatchResult {

    private Long covoiturageId;
    private String driverName;
    private String departure;
    private String destination;
    private String heureDepart;
    private String heureArrivee;
    private LocalDate date;
    private double price;
    private int availableSeats;
    private String vehicle;
    private double score;
    private String recommendation;
    private double distanceToDeparture;
    private double distanceToDestination;

    public MatchResult() {}

    public Long getCovoiturageId() { return covoiturageId; }
    public void setCovoiturageId(Long covoiturageId) { this.covoiturageId = covoiturageId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getHeureDepart() { return heureDepart; }
    public void setHeureDepart(String heureDepart) { this.heureDepart = heureDepart; }

    public String getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(String heureArrivee) { this.heureArrivee = heureArrivee; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public String getVehicle() { return vehicle; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public double getDistanceToDeparture() { return distanceToDeparture; }
    public void setDistanceToDeparture(double distanceToDeparture) { this.distanceToDeparture = distanceToDeparture; }

    public double getDistanceToDestination() { return distanceToDestination; }
    public void setDistanceToDestination(double distanceToDestination) { this.distanceToDestination = distanceToDestination; }
}

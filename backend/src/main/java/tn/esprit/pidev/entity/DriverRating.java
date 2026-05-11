package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DriverRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String driverName;
    private Long covoiturageId;
    private String route;
    private double predictedScore;
    private int stars;
    private LocalDateTime createdAt;

    public DriverRating() {}

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public Long getCovoiturageId() { return covoiturageId; }
    public void setCovoiturageId(Long covoiturageId) { this.covoiturageId = covoiturageId; }

    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }

    public double getPredictedScore() { return predictedScore; }
    public void setPredictedScore(double predictedScore) { this.predictedScore = predictedScore; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

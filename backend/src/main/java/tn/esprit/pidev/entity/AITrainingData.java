package tn.esprit.pidev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_training_data")
public class AITrainingData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String dataType;

    @Column(length = 2000)
    private String features;

    private double label;

    private String departureName;
    private String destinationName;
    private double departureLat;
    private double departureLng;
    private double destinationLat;
    private double destinationLng;

    public AITrainingData() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    public double getLabel() { return label; }
    public void setLabel(double label) { this.label = label; }

    public String getDepartureName() { return departureName; }
    public void setDepartureName(String departureName) { this.departureName = departureName; }

    public String getDestinationName() { return destinationName; }
    public void setDestinationName(String destinationName) { this.destinationName = destinationName; }

    public double getDepartureLat() { return departureLat; }
    public void setDepartureLat(double departureLat) { this.departureLat = departureLat; }

    public double getDepartureLng() { return departureLng; }
    public void setDepartureLng(double departureLng) { this.departureLng = departureLng; }

    public double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(double destinationLat) { this.destinationLat = destinationLat; }

    public double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(double destinationLng) { this.destinationLng = destinationLng; }
}

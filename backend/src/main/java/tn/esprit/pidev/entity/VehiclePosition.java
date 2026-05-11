package tn.esprit.pidev.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_positions")
public class VehiclePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", unique = true)
    private Vehicle vehicle;

    private Double latitude;
    private Double longitude;
    private Double speedKmh;
    private String heading;       // NORTH / SOUTH / EAST / WEST
    private String updatedAt;     // ISO datetime
    private boolean simulated;    // true = simulated, false = real GPS

    // Current stop index along the assigned line
    private Integer currentStopIndex;
    private String  status;       // ON_ROUTE / IDLE / OFFLINE

    public Long getId()                     { return id; }
    public void setId(Long id)              { this.id = id; }
    public Vehicle getVehicle()             { return vehicle; }
    public void setVehicle(Vehicle v)       { this.vehicle = v; }
    public Double getLatitude()             { return latitude; }
    public void setLatitude(Double v)       { this.latitude = v; }
    public Double getLongitude()            { return longitude; }
    public void setLongitude(Double v)      { this.longitude = v; }
    public Double getSpeedKmh()             { return speedKmh; }
    public void setSpeedKmh(Double v)       { this.speedKmh = v; }
    public String getHeading()              { return heading; }
    public void setHeading(String v)        { this.heading = v; }
    public String getUpdatedAt()            { return updatedAt; }
    public void setUpdatedAt(String v)      { this.updatedAt = v; }
    public boolean isSimulated()            { return simulated; }
    public void setSimulated(boolean v)     { this.simulated = v; }
    public Integer getCurrentStopIndex()    { return currentStopIndex; }
    public void setCurrentStopIndex(Integer v){ this.currentStopIndex = v; }
    public String getStatus()               { return status; }
    public void setStatus(String v)         { this.status = v; }
}

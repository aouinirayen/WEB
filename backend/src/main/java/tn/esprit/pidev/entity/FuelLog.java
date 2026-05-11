package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;
/*import jakarta.validation.constraints.*;**/

@Entity
@Table(name = "fuel_logs")
public class FuelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull(/*message = "Vehicle is required"*/)
    private Vehicle vehicle;

    @NotNull(/*message = "Liters is required"**/)
    /**@Positive(message = "Liters must be greater than 0")**/
    private Double liters;

    @NotNull(/**message = "Cost per liter is required"**/)
    /*@Positive(message = "Cost per liter must be greater than 0")*/
    private Double costPerLiter;

    @NotNull
    private Double totalCost;

    @NotNull(/*message = "Mileage at fill-up is required"**/)
    /*@PositiveOrZero(message = "Mileage must be 0 or more")*/
    private Integer mileageAtFillUp;

    /*@NotBlank(message = "Fuel date is required")*/
    private String fuelDate;

    private String station;
    private String notes;

    // ── Getters / Setters ─────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Double getLiters() { return liters; }
    public void setLiters(Double liters) { this.liters = liters; }

    public Double getCostPerLiter() { return costPerLiter; }
    public void setCostPerLiter(Double costPerLiter) { this.costPerLiter = costPerLiter; }

    public Double getTotalCost() { return totalCost; }
    public void setTotalCost(Double totalCost) { this.totalCost = totalCost; }

    public Integer getMileageAtFillUp() { return mileageAtFillUp; }
    public void setMileageAtFillUp(Integer mileageAtFillUp) { this.mileageAtFillUp = mileageAtFillUp; }

    public String getFuelDate() { return fuelDate; }
    public void setFuelDate(String fuelDate) { this.fuelDate = fuelDate; }

    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

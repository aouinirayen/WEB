package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.antlr.v4.runtime.misc.NotNull;

@Entity
@Table(name = "maintenance_part_usages")
public class MaintenancePartUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maintenance_order_id", nullable = false)
    @NotNull
    private MaintenanceOrder maintenanceOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "spare_part_id", nullable = false)
    @NotNull
    private SparePart sparePart;

    @NotNull
    @Positive(message = "Quantity used must be at least 1")
    private Integer quantityUsed;

    @NotNull
    private Double unitCostAtUsage;   // snapshot of price at time of use

    @NotNull
    private Double totalCost;         // quantityUsed × unitCostAtUsage

    private String usedDate;

    // ── Getters / Setters ─────────────────────────────────────────
    public Long getId()                             { return id; }
    public void setId(Long id)                      { this.id = id; }
    public MaintenanceOrder getMaintenanceOrder()   { return maintenanceOrder; }
    public void setMaintenanceOrder(MaintenanceOrder m) { this.maintenanceOrder = m; }
    public SparePart getSparePart()                 { return sparePart; }
    public void setSparePart(SparePart s)           { this.sparePart = s; }
    public Integer getQuantityUsed()                { return quantityUsed; }
    public void setQuantityUsed(Integer q)          { this.quantityUsed = q; }
    public Double getUnitCostAtUsage()              { return unitCostAtUsage; }
    public void setUnitCostAtUsage(Double u)        { this.unitCostAtUsage = u; }
    public Double getTotalCost()                    { return totalCost; }
    public void setTotalCost(Double t)              { this.totalCost = t; }
    public String getUsedDate()                     { return usedDate; }
    public void setUsedDate(String d)               { this.usedDate = d; }
}

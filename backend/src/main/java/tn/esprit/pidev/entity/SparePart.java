package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "spare_parts")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Part name is required")
    private String name;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    private PartCategory category;

    @NotBlank(message = "Reference code is required")
    @Column(unique = true)
    private String referenceCode;   // e.g. SP-ENG-001

    @NotNull
    @PositiveOrZero(message = "Stock quantity must be 0 or more")
    private Integer stockQuantity;

    @NotNull
    @Positive(message = "Minimum stock threshold must be positive")
    private Integer minStockThreshold; // alert triggered when stockQuantity <= this

    @NotNull
    @Positive(message = "Unit cost must be positive")
    private Double unitCost;           // cost per unit in TND

    private String supplier;
    private String notes;

    // ── Computed helper (not persisted) ──────────────────────────
    @Transient
    public boolean isLowStock() {
        return stockQuantity != null && minStockThreshold != null
            && stockQuantity <= minStockThreshold;
    }

    // ── Enum ──────────────────────────────────────────────────────
    public enum PartCategory {
        ENGINE, BRAKES, TIRES, ELECTRICAL, FILTERS,
        TRANSMISSION, BODYWORK, HVAC, OTHER
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public Long getId()                       { return id; }
    public void setId(Long id)                { this.id = id; }
    public String getName()                   { return name; }
    public void setName(String name)          { this.name = name; }
    public PartCategory getCategory()         { return category; }
    public void setCategory(PartCategory c)   { this.category = c; }
    public String getReferenceCode()          { return referenceCode; }
    public void setReferenceCode(String r)    { this.referenceCode = r; }
    public Integer getStockQuantity()         { return stockQuantity; }
    public void setStockQuantity(Integer q)   { this.stockQuantity = q; }
    public Integer getMinStockThreshold()     { return minStockThreshold; }
    public void setMinStockThreshold(Integer m){ this.minStockThreshold = m; }
    public Double getUnitCost()               { return unitCost; }
    public void setUnitCost(Double c)         { this.unitCost = c; }
    public String getSupplier()               { return supplier; }
    public void setSupplier(String s)         { this.supplier = s; }
    public String getNotes()                  { return notes; }
    public void setNotes(String n)            { this.notes = n; }
}

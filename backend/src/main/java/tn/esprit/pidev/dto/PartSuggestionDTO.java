package tn.esprit.pidev.dto;

public class PartSuggestionDTO {
    private Long partId;
    private String name;
    private String referenceCode;
    private String category;
    private Integer stockQuantity;
    private Double unitCost;
    private boolean lowStock;
    private String reason; // why this part is suggested

    public PartSuggestionDTO(Long partId, String name, String referenceCode,
                              String category, Integer stockQuantity,
                              Double unitCost, boolean lowStock, String reason) {
        this.partId = partId;
        this.name = name;
        this.referenceCode = referenceCode;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.unitCost = unitCost;
        this.lowStock = lowStock;
        this.reason = reason;
    }

    public Long getPartId()           { return partId; }
    public String getName()           { return name; }
    public String getReferenceCode()  { return referenceCode; }
    public String getCategory()       { return category; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Double getUnitCost()       { return unitCost; }
    public boolean isLowStock()       { return lowStock; }
    public String getReason()         { return reason; }
}

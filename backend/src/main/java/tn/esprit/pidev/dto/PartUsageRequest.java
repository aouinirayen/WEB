package tn.esprit.pidev.dto;

public class PartUsageRequest {
    private Long maintenanceOrderId;
    private Long sparePartId;
    private Integer quantityUsed;
    private String usedDate;

    public Long getMaintenanceOrderId()         { return maintenanceOrderId; }
    public void setMaintenanceOrderId(Long id)  { this.maintenanceOrderId = id; }
    public Long getSparePartId()                { return sparePartId; }
    public void setSparePartId(Long id)         { this.sparePartId = id; }
    public Integer getQuantityUsed()            { return quantityUsed; }
    public void setQuantityUsed(Integer q)      { this.quantityUsed = q; }
    public String getUsedDate()                 { return usedDate; }
    public void setUsedDate(String d)           { this.usedDate = d; }
}

package tn.esprit.pidev.dto;

// ===== REQUEST : Utiliser des points =====
public class RedeemRequest {
    private Long reductionId;
    private Integer pointsAUtiliser;

    public RedeemRequest() {}
    public Long getReductionId() { return reductionId; }
    public void setReductionId(Long reductionId) { this.reductionId = reductionId; }
    public Integer getPointsAUtiliser() { return pointsAUtiliser; }
    public void setPointsAUtiliser(Integer pointsAUtiliser) { this.pointsAUtiliser = pointsAUtiliser; }
}

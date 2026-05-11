package tn.esprit.pidev.dto;

import java.time.LocalDate;

// ===== REQUEST =====
public class ReductionRequest {

    private String code;
    private Double pourcentage;
    private LocalDate dateExpiration;
    private Integer pointsRequis;

    public ReductionRequest() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getPourcentage() { return pourcentage; }
    public void setPourcentage(Double pourcentage) { this.pourcentage = pourcentage; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public Integer getPointsRequis() { return pointsRequis; }
    public void setPointsRequis(Integer pointsRequis) { this.pointsRequis = pointsRequis; }
}

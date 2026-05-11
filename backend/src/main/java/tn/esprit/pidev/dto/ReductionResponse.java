package tn.esprit.pidev.dto;

import tn.esprit.pidev.entity.Reduction;

import java.time.LocalDate;

public class ReductionResponse {

    private Long id;
    private String code;
    private Double pourcentage;
    private LocalDate dateExpiration;
    private Integer pointsRequis;
    private String createdByUsername;
    private boolean estValide;

    public ReductionResponse() {}

    public static ReductionResponse fromEntity(Reduction r) {
        ReductionResponse dto = new ReductionResponse();
        dto.setId(r.getId());
        dto.setCode(r.getCode());
        dto.setPourcentage(r.getPourcentage());
        dto.setDateExpiration(r.getDateExpiration());
        dto.setPointsRequis(r.getPointsRequis());
        dto.setEstValide(r.getDateExpiration().isAfter(LocalDate.now()));
        if (r.getCreatedBy() != null) {
            dto.setCreatedByUsername(r.getCreatedBy().getUsername());
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getPourcentage() { return pourcentage; }
    public void setPourcentage(Double pourcentage) { this.pourcentage = pourcentage; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public Integer getPointsRequis() { return pointsRequis; }
    public void setPointsRequis(Integer pointsRequis) { this.pointsRequis = pointsRequis; }
    public String getCreatedByUsername() { return createdByUsername; }
    public void setCreatedByUsername(String createdByUsername) { this.createdByUsername = createdByUsername; }
    public boolean isEstValide() { return estValide; }
    public void setEstValide(boolean estValide) { this.estValide = estValide; }
}

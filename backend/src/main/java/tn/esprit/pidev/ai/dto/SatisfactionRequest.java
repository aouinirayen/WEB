package tn.esprit.pidev.ai.dto;

public class SatisfactionRequest {

    private Long covoiturageId;
    private double matchScore;
    private double prixRatio;
    private double ponctualite;
    private double placesRatio;
    private double detourKm;

    public SatisfactionRequest() {}

    public Long getCovoiturageId() { return covoiturageId; }
    public void setCovoiturageId(Long covoiturageId) { this.covoiturageId = covoiturageId; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public double getPrixRatio() { return prixRatio; }
    public void setPrixRatio(double prixRatio) { this.prixRatio = prixRatio; }

    public double getPonctualite() { return ponctualite; }
    public void setPonctualite(double ponctualite) { this.ponctualite = ponctualite; }

    public double getPlacesRatio() { return placesRatio; }
    public void setPlacesRatio(double placesRatio) { this.placesRatio = placesRatio; }

    public double getDetourKm() { return detourKm; }
    public void setDetourKm(double detourKm) { this.detourKm = detourKm; }
}

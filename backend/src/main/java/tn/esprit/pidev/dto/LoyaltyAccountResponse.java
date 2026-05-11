package tn.esprit.pidev.dto;

import tn.esprit.pidev.entity.LoyaltyAccount;
import tn.esprit.pidev.entity.LoyaltyTier;

public class LoyaltyAccountResponse {

    private Long id;
    private Integer pointsCumules;
    private LoyaltyTier niveau;
    private String passengerUsername;
    private String passengerName;
    private Integer pointsPourProchainNiveau;
    private String messageProgression;

    public LoyaltyAccountResponse() {}

    public static LoyaltyAccountResponse fromEntity(LoyaltyAccount la) {
        LoyaltyAccountResponse dto = new LoyaltyAccountResponse();
        dto.setId(la.getId());
        dto.setPointsCumules(la.getPointsCumules());
        dto.setNiveau(la.getNiveau());
        if (la.getPassenger() != null) {
            dto.setPassengerUsername(la.getPassenger().getUsername());
            dto.setPassengerName(la.getPassenger().getName());
        }
        // Calcul progression
        int pts = la.getPointsCumules();
        if (la.getNiveau() == LoyaltyTier.GOLD) {
            dto.setPointsPourProchainNiveau(0);
            dto.setMessageProgression("Niveau maximum atteint !");
        } else if (la.getNiveau() == LoyaltyTier.SILVER) {
            dto.setPointsPourProchainNiveau(500 - pts);
            dto.setMessageProgression("Encore " + (500 - pts) + " pts pour GOLD");
        } else {
            dto.setPointsPourProchainNiveau(200 - pts);
            dto.setMessageProgression("Encore " + (200 - pts) + " pts pour SILVER");
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getPointsCumules() { return pointsCumules; }
    public void setPointsCumules(Integer pointsCumules) { this.pointsCumules = pointsCumules; }
    public LoyaltyTier getNiveau() { return niveau; }
    public void setNiveau(LoyaltyTier niveau) { this.niveau = niveau; }
    public String getPassengerUsername() { return passengerUsername; }
    public void setPassengerUsername(String passengerUsername) { this.passengerUsername = passengerUsername; }
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    public Integer getPointsPourProchainNiveau() { return pointsPourProchainNiveau; }
    public void setPointsPourProchainNiveau(Integer pts) { this.pointsPourProchainNiveau = pts; }
    public String getMessageProgression() { return messageProgression; }
    public void setMessageProgression(String msg) { this.messageProgression = msg; }
}

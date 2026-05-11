package tn.esprit.pidev.dto;

import java.util.List;

public class DriverConfiance {
    private String driverName;
    private int pointsConfiance;
    private boolean conducteurDeConfiance;
    private boolean autoConfirmation;
    
    // Points détaillés
    private DetailPoints detailPoints;
    
    // 3 conditions
    private int nombreCovoituragesConfirmes;
    private int seuilCovoiturages;
    private boolean conditionCovoiturages;
    
    private int nombreAvis;
    private int avisManuel;
    private int avisIA;
    private int seuilAvis;
    private boolean conditionAvis;
    
    private double moyenneEtoiles;
    private double moyenneManuel;
    private double moyenneIA;
    private double seuilMoyenneEtoiles;
    private boolean conditionMoyenne;
    
    // Liste des avis
    private List<AvisInfo> avisList;
    
    public static class DetailPoints {
        private int covoiturages;
        private int avis;
        private int etoiles;
        
        public DetailPoints(int covoiturages, int avis, int etoiles) {
            this.covoiturages = covoiturages;
            this.avis = avis;
            this.etoiles = etoiles;
        }
        
        public int getCovoiturages() { return covoiturages; }
        public void setCovoiturages(int covoiturages) { this.covoiturages = covoiturages; }
        
        public int getAvis() { return avis; }
        public void setAvis(int avis) { this.avis = avis; }
        
        public int getEtoiles() { return etoiles; }
        public void setEtoiles(int etoiles) { this.etoiles = etoiles; }
    }
    
    public static class AvisInfo {
        private Long id;
        private int stars;
        private String source; // "MANUEL" ou "IA"
        private String route;
        private String createdAt;
        
        public AvisInfo(Long id, int stars, String source, String route, String createdAt) {
            this.id = id;
            this.stars = stars;
            this.source = source;
            this.route = route;
            this.createdAt = createdAt;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public int getStars() { return stars; }
        public void setStars(int stars) { this.stars = stars; }
        
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        
        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }
    
    // Getters and Setters
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public int getPointsConfiance() { return pointsConfiance; }
    public void setPointsConfiance(int pointsConfiance) { this.pointsConfiance = pointsConfiance; }
    
    public boolean isConducteurDeConfiance() { return conducteurDeConfiance; }
    public void setConducteurDeConfiance(boolean conducteurDeConfiance) { this.conducteurDeConfiance = conducteurDeConfiance; }
    
    public boolean isAutoConfirmation() { return autoConfirmation; }
    public void setAutoConfirmation(boolean autoConfirmation) { this.autoConfirmation = autoConfirmation; }
    
    public DetailPoints getDetailPoints() { return detailPoints; }
    public void setDetailPoints(DetailPoints detailPoints) { this.detailPoints = detailPoints; }
    
    public int getNombreCovoituragesConfirmes() { return nombreCovoituragesConfirmes; }
    public void setNombreCovoituragesConfirmes(int nombreCovoituragesConfirmes) { this.nombreCovoituragesConfirmes = nombreCovoituragesConfirmes; }
    
    public int getSeuilCovoiturages() { return seuilCovoiturages; }
    public void setSeuilCovoiturages(int seuilCovoiturages) { this.seuilCovoiturages = seuilCovoiturages; }
    
    public boolean isConditionCovoiturages() { return conditionCovoiturages; }
    public void setConditionCovoiturages(boolean conditionCovoiturages) { this.conditionCovoiturages = conditionCovoiturages; }
    
    public int getNombreAvis() { return nombreAvis; }
    public void setNombreAvis(int nombreAvis) { this.nombreAvis = nombreAvis; }
    
    public int getAvisManuel() { return avisManuel; }
    public void setAvisManuel(int avisManuel) { this.avisManuel = avisManuel; }
    
    public int getAvisIA() { return avisIA; }
    public void setAvisIA(int avisIA) { this.avisIA = avisIA; }
    
    public int getSeuilAvis() { return seuilAvis; }
    public void setSeuilAvis(int seuilAvis) { this.seuilAvis = seuilAvis; }
    
    public boolean isConditionAvis() { return conditionAvis; }
    public void setConditionAvis(boolean conditionAvis) { this.conditionAvis = conditionAvis; }
    
    public double getMoyenneEtoiles() { return moyenneEtoiles; }
    public void setMoyenneEtoiles(double moyenneEtoiles) { this.moyenneEtoiles = moyenneEtoiles; }
    
    public double getMoyenneManuel() { return moyenneManuel; }
    public void setMoyenneManuel(double moyenneManuel) { this.moyenneManuel = moyenneManuel; }
    
    public double getMoyenneIA() { return moyenneIA; }
    public void setMoyenneIA(double moyenneIA) { this.moyenneIA = moyenneIA; }
    
    public double getSeuilMoyenneEtoiles() { return seuilMoyenneEtoiles; }
    public void setSeuilMoyenneEtoiles(double seuilMoyenneEtoiles) { this.seuilMoyenneEtoiles = seuilMoyenneEtoiles; }
    
    public boolean isConditionMoyenne() { return conditionMoyenne; }
    public void setConditionMoyenne(boolean conditionMoyenne) { this.conditionMoyenne = conditionMoyenne; }
    
    public List<AvisInfo> getAvisList() { return avisList; }
    public void setAvisList(List<AvisInfo> avisList) { this.avisList = avisList; }
}

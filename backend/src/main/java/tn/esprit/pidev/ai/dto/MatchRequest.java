package tn.esprit.pidev.ai.dto;

public class MatchRequest {

    private double passengerLat;
    private double passengerLng;
    private double destLat;
    private double destLng;
    private String heureVoulue;
    private double budgetMax;

    public MatchRequest() {}

    public double getPassengerLat() { return passengerLat; }
    public void setPassengerLat(double passengerLat) { this.passengerLat = passengerLat; }

    public double getPassengerLng() { return passengerLng; }
    public void setPassengerLng(double passengerLng) { this.passengerLng = passengerLng; }

    public double getDestLat() { return destLat; }
    public void setDestLat(double destLat) { this.destLat = destLat; }

    public double getDestLng() { return destLng; }
    public void setDestLng(double destLng) { this.destLng = destLng; }

    public String getHeureVoulue() { return heureVoulue; }
    public void setHeureVoulue(String heureVoulue) { this.heureVoulue = heureVoulue; }

    public double getBudgetMax() { return budgetMax; }
    public void setBudgetMax(double budgetMax) { this.budgetMax = budgetMax; }
}

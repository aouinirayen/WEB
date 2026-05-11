package tn.esprit.pidev.ai.dto;

public class CancellationRequest {

    private double prix;
    private double distanceKm;
    private int joursAvant;
    private double heure;
    private int nbPlaces;

    public CancellationRequest() {}

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public int getJoursAvant() { return joursAvant; }
    public void setJoursAvant(int joursAvant) { this.joursAvant = joursAvant; }

    public double getHeure() { return heure; }
    public void setHeure(double heure) { this.heure = heure; }

    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }
}

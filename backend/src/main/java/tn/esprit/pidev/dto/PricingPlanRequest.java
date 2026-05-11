package tn.esprit.pidev.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import tn.esprit.pidev.entity.PricingType;
import tn.esprit.pidev.entity.TransportType;  // ← import ajouté

public class PricingPlanRequest {

    private String      nom;
    private String      description;
    private Double      prix;
    @JsonAlias("dureeEnMois")
    private Integer     dureeEnJours;
    private PricingType type;
    private TransportType transportType;  // ← déjà déclaré mais sans getter/setter

    public PricingPlanRequest() {}

    public String getNom()                     { return nom; }
    public void setNom(String nom)             { this.nom = nom; }
    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }
    public Double getPrix()                    { return prix; }
    public void setPrix(Double prix)           { this.prix = prix; }
    public Integer getDureeEnJours()           { return dureeEnJours; }
    public void setDureeEnJours(Integer d)     { this.dureeEnJours = d; }
    public PricingType getType()               { return type; }
    public void setType(PricingType type)      { this.type = type; }
    // ── ajoutés ──
    public TransportType getTransportType()              { return transportType; }
    public void setTransportType(TransportType t)        { this.transportType = t; }
}
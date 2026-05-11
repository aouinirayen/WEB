package tn.esprit.pidev.dto;

import tn.esprit.pidev.entity.PricingPlan;
import tn.esprit.pidev.entity.PricingType;
import tn.esprit.pidev.entity.TransportType;  // ← import ajouté

public class PricingPlanResponse {

    private Long          id;
    private String        nom;
    private String        description;
    private Double        prix;
    private Integer       dureeEnJours;
    private PricingType   type;
    private TransportType transportType;       // ← champ ajouté
    private String        createdByUsername;

    public PricingPlanResponse() {}

    public static PricingPlanResponse fromEntity(PricingPlan p) {
        PricingPlanResponse dto = new PricingPlanResponse();
        dto.setId(p.getId());
        dto.setNom(p.getNom());
        dto.setDescription(p.getDescription());
        dto.setPrix(p.getPrix());
        dto.setDureeEnJours(p.getDureeEnJours());
        dto.setType(p.getType());
        dto.setTransportType(p.getTransportType()); // ← mapping ajouté
        if (p.getCreatedBy() != null) {
            dto.setCreatedByUsername(p.getCreatedBy().getUsername());
        }
        return dto;
    }

    // Getters & Setters
    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getNom()                           { return nom; }
    public void setNom(String nom)                   { this.nom = nom; }
    public String getDescription()                   { return description; }
    public void setDescription(String description)   { this.description = description; }
    public Double getPrix()                          { return prix; }
    public void setPrix(Double prix)                 { this.prix = prix; }
    public Integer getDureeEnJours()                 { return dureeEnJours; }
    public void setDureeEnJours(Integer dureeEnJours){ this.dureeEnJours = dureeEnJours; }
    public PricingType getType()                     { return type; }
    public void setType(PricingType type)            { this.type = type; }
    public TransportType getTransportType()          { return transportType; }        // ← ajouté
    public void setTransportType(TransportType t)    { this.transportType = t; }      // ← ajouté
    public String getCreatedByUsername()             { return createdByUsername; }
    public void setCreatedByUsername(String u)       { this.createdByUsername = u; }
}
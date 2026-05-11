package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "pricing_plan")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PricingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double prix;

    // Compat DB: on conserve la colonne existante duree_en_mois, mais la valeur représente désormais des jours.
    @Column(name = "duree_en_mois", nullable = false)
    private Integer dureeEnJours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private TransportType transportType;

    // Lien avec User (Operator qui a créé ce plan)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_operator_id")
    @JsonIgnore
    private User createdBy;

    @OneToMany(mappedBy = "pricingPlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Subscription> subscriptions;

    // Constructors
    public PricingPlan() {}

    public PricingPlan(String nom, String description, Double prix, Integer dureeEnJours, PricingType type) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeEnJours = dureeEnJours;
        this.type = type;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    public Integer getDureeEnJours() { return dureeEnJours; }
    public void setDureeEnJours(Integer dureeEnJours) { this.dureeEnJours = dureeEnJours; }
    public PricingType getType() { return type; }
    public void setType(PricingType type) { this.type = type; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public List<Subscription> getSubscriptions() { return subscriptions; }
    public void setSubscriptions(List<Subscription> subscriptions) { this.subscriptions = subscriptions; }
    public TransportType getTransportType() { return transportType; }
    public void setTransportType(TransportType transportType) { this.transportType = transportType; }
}

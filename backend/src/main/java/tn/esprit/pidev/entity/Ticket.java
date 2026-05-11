package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private double price;
    private String description;
    private String validity;

    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    private int quantiteDisponible;

    @JsonProperty("disponible")
    private boolean disponible = true;

    private String lieuDepart;
    private String destination;
    private String heureDepart;

    public Ticket() {}

    public Long getId() { return id; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getValidity() { return validity; }
    public TransportType getTransportType() { return transportType; }
    public int getQuantiteDisponible() { return quantiteDisponible; }
    @JsonProperty("disponible")
    public boolean isDisponible() { return disponible; }
    public String getLieuDepart() { return lieuDepart; }
    public String getDestination() { return destination; }
    public String getHeureDepart() { return heureDepart; }

    public void setId(Long id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setValidity(String validity) { this.validity = validity; }
    public void setTransportType(TransportType transportType) { this.transportType = transportType; }
    public void setQuantiteDisponible(int quantiteDisponible) {
        this.quantiteDisponible = quantiteDisponible;
        this.disponible = quantiteDisponible > 0;
    }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public void setLieuDepart(String lieuDepart) { this.lieuDepart = lieuDepart; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setHeureDepart(String heureDepart) { this.heureDepart = heureDepart; }
}

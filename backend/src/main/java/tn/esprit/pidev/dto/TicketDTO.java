package tn.esprit.pidev.dto;

public class TicketDTO {

    private String type;
    private double price;
    private String description;
    private String validity;
    private String transportType;
    private int quantiteDisponible;
    private String lieuDepart;
    private String destination;
    private String heureDepart;

    public TicketDTO() {}

    public String getType() { return type; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getValidity() { return validity; }
    public String getTransportType() { return transportType; }
    public int getQuantiteDisponible() { return quantiteDisponible; }
    public String getLieuDepart() { return lieuDepart; }
    public String getDestination() { return destination; }
    public String getHeureDepart() { return heureDepart; }

    public void setType(String type) { this.type = type; }
    public void setPrice(double price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setValidity(String validity) { this.validity = validity; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public void setQuantiteDisponible(int quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }
    public void setLieuDepart(String lieuDepart) { this.lieuDepart = lieuDepart; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setHeureDepart(String heureDepart) { this.heureDepart = heureDepart; }
}

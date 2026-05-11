package tn.esprit.pidev.dto;

import java.time.LocalDate;

public class CovoiturageDTO {

    private String driverName;
    private String departure;
    private String destination;
    private LocalDate date;
    private String heureDepart;
    private String heureArrivee;
    private double price;
    private int availableSeats;
    private String vehicle;
    private String status;
    private Double departureLat;
    private Double departureLng;
    private Double destinationLat;
    private Double destinationLng;

    public CovoiturageDTO() {}

    public String getDriverName() { return driverName; }
    public String getDeparture() { return departure; }
    public String getDestination() { return destination; }
    public LocalDate getDate() { return date; }
    public String getHeureDepart() { return heureDepart; }
    public String getHeureArrivee() { return heureArrivee; }
    public double getPrice() { return price; }
    public int getAvailableSeats() { return availableSeats; }
    public String getVehicle() { return vehicle; }
    public String getStatus() { return status; }
    public Double getDepartureLat() { return departureLat; }
    public Double getDepartureLng() { return departureLng; }
    public Double getDestinationLat() { return destinationLat; }
    public Double getDestinationLng() { return destinationLng; }

    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setDeparture(String departure) { this.departure = departure; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setHeureDepart(String heureDepart) { this.heureDepart = heureDepart; }
    public void setHeureArrivee(String heureArrivee) { this.heureArrivee = heureArrivee; }
    public void setPrice(double price) { this.price = price; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public void setVehicle(String vehicle) { this.vehicle = vehicle; }
    public void setStatus(String status) { this.status = status; }
    public void setDepartureLat(Double departureLat) { this.departureLat = departureLat; }
    public void setDepartureLng(Double departureLng) { this.departureLng = departureLng; }
    public void setDestinationLat(Double destinationLat) { this.destinationLat = destinationLat; }
    public void setDestinationLng(Double destinationLng) { this.destinationLng = destinationLng; }
}

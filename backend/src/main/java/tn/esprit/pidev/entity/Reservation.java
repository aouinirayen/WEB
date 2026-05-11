package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientName;
    private String phone;
    private String email;
    private int seatsReserved;
    private LocalDate bookingDate;
    private String status;
    private Long covoiturageId;

    private Double clientLat;
    private Double clientLng;
    private String clientAddress;
    private Boolean displacementRequested;
    private Double displacementPrice;

    public Reservation() {}

    public Long getId() { return id; }
    public String getClientName() { return clientName; }
    public String getPhone() { return phone; }
    public int getSeatsReserved() { return seatsReserved; }
    public LocalDate getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }
    public String getEmail() { return email; }
    public Long getCovoiturageId() { return covoiturageId; }
    public Double getClientLat() { return clientLat; }
    public Double getClientLng() { return clientLng; }
    public String getClientAddress() { return clientAddress; }
    public Boolean getDisplacementRequested() { return displacementRequested; }
    public Double getDisplacementPrice() { return displacementPrice; }

    public void setId(Long id) { this.id = id; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSeatsReserved(int seatsReserved) { this.seatsReserved = seatsReserved; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public void setStatus(String status) { this.status = status; }
    public void setEmail(String email) { this.email = email; }
    public void setCovoiturageId(Long covoiturageId) { this.covoiturageId = covoiturageId; }
    public void setClientLat(Double clientLat) { this.clientLat = clientLat; }
    public void setClientLng(Double clientLng) { this.clientLng = clientLng; }
    public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }
    public void setDisplacementRequested(Boolean displacementRequested) { this.displacementRequested = displacementRequested; }
    public void setDisplacementPrice(Double displacementPrice) { this.displacementPrice = displacementPrice; }
}

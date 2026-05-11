package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "loyalty_account")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer pointsCumules;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoyaltyTier niveau;

    // Lien direct avec User (Passenger)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "passenger_id", nullable = false, unique = true)
    private User passenger;

    @OneToMany(mappedBy = "loyaltyAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PointTransaction> transactions;

    // Constructors
    public LoyaltyAccount() {}

    public LoyaltyAccount(User passenger) {
        this.passenger = passenger;
        this.pointsCumules = 0;
        this.niveau = LoyaltyTier.BRONZE;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public Integer getPointsCumules() { return pointsCumules; }
    public void setPointsCumules(Integer pointsCumules) { this.pointsCumules = pointsCumules; }
    public LoyaltyTier getNiveau() { return niveau; }
    public void setNiveau(LoyaltyTier niveau) { this.niveau = niveau; }
    public User getPassenger() { return passenger; }
    public void setPassenger(User passenger) { this.passenger = passenger; }
    public List<PointTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<PointTransaction> transactions) { this.transactions = transactions; }
}

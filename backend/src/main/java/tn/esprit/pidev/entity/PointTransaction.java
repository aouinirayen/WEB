package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_transaction")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer points;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_account_id", nullable = false)
    @JsonIgnore
    private LoyaltyAccount loyaltyAccount;

    // Constructors
    public PointTransaction() {}

    public PointTransaction(Integer points, TransactionType type,
                            LocalDateTime date, String description, LoyaltyAccount loyaltyAccount) {
        this.points = points;
        this.type = type;
        this.date = date;
        this.description = description;
        this.loyaltyAccount = loyaltyAccount;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LoyaltyAccount getLoyaltyAccount() { return loyaltyAccount; }
    public void setLoyaltyAccount(LoyaltyAccount loyaltyAccount) { this.loyaltyAccount = loyaltyAccount; }
}

package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "reduction")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double pourcentage;

    @Column(nullable = false)
    private LocalDate dateExpiration;

    @Column(nullable = false)
    private Integer pointsRequis;

    // Lien avec Operator qui a créé la réduction
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_operator_id")
    @JsonIgnore
    private User createdBy;

    // Constructors
    public Reduction() {}

    public Reduction(String code, Double pourcentage, LocalDate dateExpiration,
                     Integer pointsRequis, User createdBy) {
        this.code = code;
        this.pourcentage = pourcentage;
        this.dateExpiration = dateExpiration;
        this.pointsRequis = pointsRequis;
        this.createdBy = createdBy;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Double getPourcentage() { return pourcentage; }
    public void setPourcentage(Double pourcentage) { this.pourcentage = pourcentage; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public Integer getPointsRequis() { return pointsRequis; }
    public void setPointsRequis(Integer pointsRequis) { this.pointsRequis = pointsRequis; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
}

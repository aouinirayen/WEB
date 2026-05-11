package tn.esprit.pidev.entity;
import tn.esprit.pidev.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "partner_contract")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    @Enumerated(EnumType.STRING)
    private ContractStatus status;

    @NotNull(message = "Start date is required")
    // @FutureOrPresent removed: blocks update of existing contracts with past startDate
    private Date startDate;
    @NotNull(message = "End date is required")
    private Date endDate;

    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private LocalDateTime createdAt;

    // Digital Signature Fields
    @Column(name = "signature_hash", length = 64)
    private String signatureHash;

    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    @Column(name = "signed_by")
    private String signedBy;

    @Column(name = "is_signed")
    private Boolean isSigned = false;

    @Column(name = "signature_valid")
    private Boolean signatureValid = false;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isSigned == null) this.isSigned = false;
        if (this.signatureValid == null) this.signatureValid = false;
    }
}


package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for legal documents uploaded by users
 * Tracks document lifecycle: upload, verification, expiry, rejection
 */
@Entity
@Table(name = "legal_documents", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_document_type_id", columnList = "document_type_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_expiry_date", columnList = "expiry_date"),
    @Index(name = "idx_user_status_expiry", columnList = "user_id,status,expiry_date")
})
public class LegalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId; // FK to User table

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType; // Relationship to DocumentType

    @Column(nullable = false)
    private String documentUrl; // Path/URL where file is stored

    @Column(nullable = false)
    private String fileHash; // SHA-256 hash for integrity check

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate; // Nullable, depends on document type

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatusEnum status = DocumentStatusEnum.PENDING;


    // Constructors
    public LegalDocument() {
        this.uploadDate = LocalDateTime.now();
        this.status = DocumentStatusEnum.PENDING;
    }

    public LegalDocument(Long userId, DocumentType documentType, String documentUrl, String fileHash, LocalDateTime expiryDate) {
        this();
        this.userId = userId;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.fileHash = fileHash;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Long getDocumentTypeId() {
        return documentType != null ? documentType.getId() : null;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public DocumentStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DocumentStatusEnum status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "LegalDocument{" +
                "id=" + id +
                ", userId=" + userId +
                ", documentTypeId=" + getDocumentTypeId() +
                ", status=" + status +
                ", uploadDate=" + uploadDate +
                ", expiryDate=" + expiryDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LegalDocument that = (LegalDocument) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}


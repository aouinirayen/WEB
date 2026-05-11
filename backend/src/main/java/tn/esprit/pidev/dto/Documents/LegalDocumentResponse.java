package tn.esprit.pidev.dto.Documents;

import java.time.LocalDateTime;
import tn.esprit.pidev.entity.DocumentStatusEnum;

/**
 * DTO for legal document responses (read-only)
 */
public class LegalDocumentResponse {

    private Long id;
    private Long userId;
    private String username; // New field for frontend
    private Long documentTypeId;
    private String documentUrl;
    private LocalDateTime uploadDate;
    private LocalDateTime expiryDate;
    private DocumentStatusEnum status;
    private DocumentTypeResponse documentType; // Nested DTO

    public LegalDocumentResponse() {}

    public LegalDocumentResponse(Long id, Long userId, String username, Long documentTypeId, String documentUrl,
                                 LocalDateTime uploadDate, LocalDateTime expiryDate,
                                 DocumentStatusEnum status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.documentTypeId = documentTypeId;
        this.documentUrl = documentUrl;
        this.uploadDate = uploadDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    // Legacy constructor without username for backward compatibility
    public LegalDocumentResponse(Long id, Long userId, Long documentTypeId, String documentUrl,
                                 LocalDateTime uploadDate, LocalDateTime expiryDate,
                                 DocumentStatusEnum status) {
        this.id = id;
        this.userId = userId;
        this.documentTypeId = documentTypeId;
        this.documentUrl = documentUrl;
        this.uploadDate = uploadDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
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


    public DocumentTypeResponse getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentTypeResponse documentType) {
        this.documentType = documentType;
    }

    @Override
    public String toString() {
        return "LegalDocumentResponse{" +
                "id=" + id +
                ", userId=" + userId +
                ", documentTypeId=" + documentTypeId +
                ", status=" + status +
                ", uploadDate=" + uploadDate +
                ", expiryDate=" + expiryDate +
                '}';
    }
}


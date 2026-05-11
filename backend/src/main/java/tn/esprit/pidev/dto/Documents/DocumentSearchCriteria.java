package tn.esprit.pidev.dto.Documents;

import jakarta.validation.constraints.NotNull;
import tn.esprit.pidev.entity.DocumentStatusEnum;

/**
 * DTO for searching and filtering legal documents
 */
public class DocumentSearchCriteria {

    private Long userId;
    private Long documentTypeId;
    private DocumentStatusEnum status;
    private int page = 0;
    private int size = 10;
    private String sortBy = "uploadDate";
    private String sortDir = "desc";
    private Boolean includeDeleted = false;

    public DocumentSearchCriteria() {}

    public DocumentSearchCriteria(Long userId, Long documentTypeId, DocumentStatusEnum status,
                                  int page, int size, String sortBy, String sortDir) {
        this.userId = userId;
        this.documentTypeId = documentTypeId;
        this.status = status;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDir = sortDir;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public DocumentStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DocumentStatusEnum status) {
        this.status = status;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.max(1, Math.min(size, 100)); // Max 100 per page
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public Boolean getIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }
}


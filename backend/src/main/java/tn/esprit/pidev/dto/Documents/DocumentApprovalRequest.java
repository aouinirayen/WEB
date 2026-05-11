package tn.esprit.pidev.dto.Documents;

import jakarta.validation.constraints.NotNull;
import tn.esprit.pidev.entity.DocumentStatusEnum;

/**
 * DTO for approving/rejecting documents by admin
 */
public class DocumentApprovalRequest {

    @NotNull(message = "Status is required")
    private DocumentStatusEnum status;

    private String rejectionReason; // Required if status is REJECTED or REQUEST_UPDATE

    public DocumentApprovalRequest() {}

    public DocumentApprovalRequest(DocumentStatusEnum status, String rejectionReason) {
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public DocumentStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DocumentStatusEnum status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}


package tn.esprit.pidev.entity;

/**
 * Enum for legal document status
 * PENDING - Waiting for admin review
 * VALID - Approved by admin
 * REJECTED - Denied by admin
 * EXPIRED - Past expiry date
 * REQUEST_UPDATE - Admin asking user to re-upload
 */
public enum DocumentStatusEnum {
    PENDING("Waiting for verification"),
    VALID("Verified and valid"),
    REJECTED("Rejected by admin"),
    EXPIRED("Document has expired"),
    REQUEST_UPDATE("Update requested by admin");

    private final String description;

    DocumentStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


package tn.esprit.pidev.dto.Documents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating/updating document types
 */
public class DocumentTypeCreateRequest {

    @NotBlank(message = "Document type name is required")
    private String name;

    private String description;

    @NotNull(message = "requiresExpiry flag is required")
    private Boolean requiresExpiry = true;

    private String allowedRoles; // Comma-separated: "PASSENGER,DRIVER"

    public DocumentTypeCreateRequest() {}

    public DocumentTypeCreateRequest(String name, String description, Boolean requiresExpiry, String allowedRoles) {
        this.name = name;
        this.description = description;
        this.requiresExpiry = requiresExpiry;
        this.allowedRoles = allowedRoles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequiresExpiry() {
        return requiresExpiry;
    }

    public void setRequiresExpiry(Boolean requiresExpiry) {
        this.requiresExpiry = requiresExpiry;
    }

    public String getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(String allowedRoles) {
        this.allowedRoles = allowedRoles;
    }
}

package tn.esprit.pidev.dto.Documents;

/**
 * DTO for document type responses
 */
public class DocumentTypeResponse {

    private Long id;
    private String name;
    private String description;
    private Boolean requiresExpiry;
    private String allowedRoles;

    public DocumentTypeResponse() {}

    public DocumentTypeResponse(Long id, String name, String description, Boolean requiresExpiry,
                                String allowedRoles) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiresExpiry = requiresExpiry;
        this.allowedRoles = allowedRoles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    @Override
    public String toString() {
        return "DocumentTypeResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

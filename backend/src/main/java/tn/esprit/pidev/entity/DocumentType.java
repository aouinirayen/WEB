package tn.esprit.pidev.entity;

import jakarta.persistence.*;

/**
 * Entity for document types that users must upload (passport, license, etc.)
 * Admin manages available document types
 */
@Entity
@Table(name = "document_types")
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Passport", "Driver License"

    @Column(columnDefinition = "TEXT")
    private String description; // e.g., "Valid national or international passport"

    @Column(nullable = false)
    private Boolean requiresExpiry = true; // Whether this document type needs an expiry date

    /**
     * JSON array of role enums that are allowed to upload this document type
     * Example: ["PASSENGER", "DRIVER"]
     * Stored as comma-separated string for simplicity
     */
    @Column(columnDefinition = "VARCHAR(255)")
    private String allowedRoles; // Comma-separated roles: "PASSENGER,DRIVER"

    // Constructors
    public DocumentType() {
        this.requiresExpiry = true;
    }

    public DocumentType(String name, String description, Boolean requiresExpiry, String allowedRoles) {
        this();
        this.name = name;
        this.description = description;
        this.requiresExpiry = requiresExpiry;
        this.allowedRoles = allowedRoles;
    }

    // Getters and Setters
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
        return "DocumentType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", requiresExpiry=" + requiresExpiry +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentType that = (DocumentType) o;
        return java.util.Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}

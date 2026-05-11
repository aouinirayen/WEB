package tn.esprit.pidev.dto.users;

import java.time.LocalDateTime;

/**
 * DTO for user profile response
 * Includes all profile information that a user can view about themselves
 */
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Long cin;
    private String role;
    private String photoPath;
    private String photoContentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ProfileResponse() {
    }

    public ProfileResponse(Long id, String username, String email, String name, Long cin, 
                          String role, String photoPath, String photoContentType, LocalDateTime createdAt, 
                          LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.cin = cin;
        this.role = role;
        this.photoPath = photoPath;
        this.photoContentType = photoContentType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCin() {
        return cin;
    }

    public void setCin(Long cin) {
        this.cin = cin;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getPhotoContentType() {
        return photoContentType;
    }

    public void setPhotoContentType(String photoContentType) {
        this.photoContentType = photoContentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


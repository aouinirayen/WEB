package tn.esprit.pidev.dto.users;

import java.time.LocalDateTime;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private Long cin;
    private String role;
    private String photoContentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean enabled;
    private LocalDateTime inactivatedUntil;

    public UserResponse(Long id, String username, String email, String name, Long cin, String role, String photoContentType, LocalDateTime createdAt, LocalDateTime updatedAt, boolean enabled) {
        this(id, username, email, name, cin, role, photoContentType, createdAt, updatedAt, enabled, null);
    }

    public UserResponse(Long id, String username, String email, String name, Long cin, String role, String photoContentType, LocalDateTime createdAt, LocalDateTime updatedAt, boolean enabled, LocalDateTime inactivatedUntil) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.cin = cin;
        this.role = role;
        this.photoContentType = photoContentType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.enabled = enabled;
        this.inactivatedUntil = inactivatedUntil;
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getInactivatedUntil() {
        return inactivatedUntil;
    }

    public void setInactivatedUntil(LocalDateTime inactivatedUntil) {
        this.inactivatedUntil = inactivatedUntil;
    }
}

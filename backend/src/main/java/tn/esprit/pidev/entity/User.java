package tn.esprit.pidev.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Specifies the table name in the database

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String name;          // renamed from fullName to match spec

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role;        // new field, must be non-null

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private TransportType transportType; // null pour ADMIN, AGENT, PASSENGER

    @Column(unique = true)
    private Long cin;             // new field, often unique

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "inactivated_until")
    private LocalDateTime inactivatedUntil;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Column(name = "photo_content_type")
    private String photoContentType;

    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String username, String password, String email, String name, RoleEnum role) {
        this(); // sets timestamps
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    // Constructor for OPERATOR with transport type
    public User(String username, String password, String email, String name, RoleEnum role, TransportType transportType) {
        this(username, password, email, name, role);
        if (role == RoleEnum.OPERATOR) {
            this.transportType = transportType;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPhotoContentType() {
        return photoContentType;
    }

    public void setPhotoContentType(String photoContentType) {
        this.photoContentType = photoContentType;
        this.updatedAt = LocalDateTime.now();
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        // Only OPERATOR role can have transport_type attribute
        if (this.role != RoleEnum.OPERATOR && transportType != null) {
            throw new IllegalArgumentException("Transport type can only be set for OPERATOR role");
        }
        this.transportType = transportType;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getCin() {
        return cin;
    }

    public void setCin(Long cin) {
        this.cin = cin;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getInactivatedUntil() {
        return inactivatedUntil;
    }

    public void setInactivatedUntil(LocalDateTime inactivatedUntil) {
        this.inactivatedUntil = inactivatedUntil;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    public void setPasswordResetToken(String passwordResetToken) {
        this.passwordResetToken = passwordResetToken;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getPasswordResetTokenExpiry() {
        return passwordResetTokenExpiry;
    }

    public void setPasswordResetTokenExpiry(LocalDateTime passwordResetTokenExpiry) {
        this.passwordResetTokenExpiry = passwordResetTokenExpiry;
        this.updatedAt = LocalDateTime.now();
    }

    // Optional: override toString, equals, hashCode
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return java.util.Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

}

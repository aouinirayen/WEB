package tn.esprit.pidev.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.TransportType;

public class UserUpdateRequest {
    @JsonProperty("username")
    @NotBlank(message = "Username is required")
    private String username;

    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @JsonProperty("name")
    @NotBlank(message = "Name is required")
    private String name;

    @JsonProperty("role")
    @NotNull(message = "Role is required")
    private RoleEnum role;

    @JsonProperty("cin")
    private Long cin;  // Optional field

    @JsonProperty("enabled")
    private Boolean enabled;  // Optional field

    @JsonProperty("transportType")
    private TransportType transportType;  // Required for OPERATOR, optional for others

    // Getters and Setters
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

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public Long getCin() {
        return cin;
    }

    public void setCin(Long cin) {
        this.cin = cin;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
}

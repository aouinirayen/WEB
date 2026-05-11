package tn.esprit.pidev.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for user profile updates
 * Non-admin users can update: email, name, and cin
 * They cannot change: username, role, or enabled status
 *
 * NOTE: Username cannot be changed here as it would invalidate JWT tokens.
 * Usernames can only be changed by admins through /api/admin/users/{id} endpoint
 * or by creating a new account.
 */
public class ProfileUpdateRequest {
    @JsonProperty("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @JsonProperty("name")
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    @JsonProperty("cin")
    private Long cin;  // Optional field

    // Constructors
    public ProfileUpdateRequest() {
    }

    public ProfileUpdateRequest(String email, String name, Long cin) {
        this.email = email;
        this.name = name;
        this.cin = cin;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Trim and normalize email
        this.email = email != null ? email.trim() : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        // Trim and normalize name
        this.name = name != null ? name.trim() : null;
    }


    public Long getCin() {
        return cin;
    }

    public void setCin(Long cin) {
        this.cin = cin;
    }
}


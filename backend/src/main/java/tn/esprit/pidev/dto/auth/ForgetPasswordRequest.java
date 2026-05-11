package tn.esprit.pidev.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for forget password request
 */
public class ForgetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @JsonProperty("email")
    private String email;

    // Constructors
    public ForgetPasswordRequest() {}

    public ForgetPasswordRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ForgetPasswordRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}


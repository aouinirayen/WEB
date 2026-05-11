package tn.esprit.pidev.dto;

import tn.esprit.pidev.enums.PartnerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Industry sector is required")
    private String industrySector;

    @NotBlank(message = "Partnership type is required")
    private String partnershipType;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String website;
    private String logo;

    @NotNull(message = "Status is required")
    private PartnerStatus status;

    private Long organizationId;
}

package tn.esprit.pidev.entity;

import tn.esprit.pidev.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "partner")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Partner name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.]+$", message = "Name contains invalid characters")
    private String name;

    @NotBlank(message = "Industry sector is required")
    @Size(min = 2, max = 50, message = "Industry sector must be between 2 and 50 characters")
    private String industrySector;

    @NotBlank(message = "Partnership type is required")
    @Size(min = 2, max = 50, message = "Partnership type must be between 2 and 50 characters")
    private String partnershipType;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+216[0-9]{8}$",
             message = "Phone number must be a valid Tunisian number (+216XXXXXXXX)")
    private String phoneNumber;

    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$",
             message = "Invalid website URL format")
    private String website;

    private String logo;

    @NotNull(message = "Partner status is required")
    @Enumerated(EnumType.STRING)
    private PartnerStatus status;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<PartnerContract> contracts;
}

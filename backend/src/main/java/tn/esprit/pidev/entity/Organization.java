package tn.esprit.pidev.entity;
import tn.esprit.pidev.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.''ÃƒÂ ÃƒÂ¢ÃƒÂ¤ÃƒÂ©ÃƒÂ¨ÃƒÂªÃƒÂ«ÃƒÂ®ÃƒÂ¯ÃƒÂ´ÃƒÂ¶ÃƒÂ¹ÃƒÂ»ÃƒÂ¼ÃƒÂ§]+$",
             message = "Name contains invalid characters")
    private String name;

    @NotBlank(message = "Acronym is required")
    @Size(min = 2, max = 10, message = "Acronym must be between 2 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Acronym must contain only uppercase letters and digits")
    private String acronyme;

    @NotBlank(message = "Transport type is required")
    @Size(min = 2, max = 50, message = "Transport type must be between 2 and 50 characters")
    private String transportType;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+216[0-9]{8}$",
             message = "Phone number must be a valid Tunisian number (+216XXXXXXXX)")
    private String phoneNumber;

    @Pattern(regexp = "^$|^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$",
             message = "Invalid website URL format")
    private String website;

    @Column(columnDefinition = "TEXT")
    private String logo;
    private String region;

    @NotNull(message = "Organization type is required")
    @Enumerated(EnumType.STRING)
    private OrgType type;

    @NotNull(message = "Organization status is required")
    @Enumerated(EnumType.STRING)
    private OrgStatus status;

    @NotNull(message = "Coverage type is required")
    @Enumerated(EnumType.STRING)
    private CoverageType coverageType;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<OperatorZone> zones;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<PartnerContract> contracts;
}
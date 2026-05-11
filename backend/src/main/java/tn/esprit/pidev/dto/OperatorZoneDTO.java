package tn.esprit.pidev.dto;

import tn.esprit.pidev.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorZoneDTO {
    private Long id;

    @NotBlank(message = "Le gouvernorat est obligatoire")
    private String governorate;

    @NotNull(message = "La région est obligatoire")
    private Region region;

    private Boolean isHeadquarter;

    @NotNull(message = "Le type de couverture est obligatoire")
    private CoverageType coverageType;

    @NotNull(message = "L'organisation est obligatoire")
    private Long organizationId;
}

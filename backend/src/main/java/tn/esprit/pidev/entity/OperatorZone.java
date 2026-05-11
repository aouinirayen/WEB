package tn.esprit.pidev.entity;

import tn.esprit.pidev.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "operator_zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatorZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String governorate;

    @Enumerated(EnumType.STRING)
    private Region region;

    private Boolean isHeadquarter;

    @Enumerated(EnumType.STRING)
    private CoverageType coverageType;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
}

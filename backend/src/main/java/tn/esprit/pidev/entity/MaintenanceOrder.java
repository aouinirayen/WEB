package tn.esprit.pidev.entity;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pidev.entity.enums.MaintenanceStatus;
import tn.esprit.pidev.entity.enums.MaintenanceType;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    private MaintenanceType type;

    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private Double cost;
    private String description;
    private String technicianName;
}
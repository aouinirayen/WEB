package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonBackReference("schedule-trips")
    private Schedule schedule;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer delayMinutes;
    private Boolean completed;
}
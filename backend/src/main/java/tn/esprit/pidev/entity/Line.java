package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pidev.entity.enums.LineStatus;
import tn.esprit.pidev.entity.enums.TransportMode;
import java.util.List;

@Entity
@Table(name = "transit_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Line {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code;

    @Enumerated(EnumType.STRING)
    private TransportMode mode;

    @Enumerated(EnumType.STRING)
    private LineStatus status;

    @OneToMany(mappedBy = "line",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonManagedReference("line-stops")

    private List<Stop> stops;

    @OneToMany(mappedBy = "line",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonManagedReference("line-schedules")
    private List<Schedule> schedules;
}
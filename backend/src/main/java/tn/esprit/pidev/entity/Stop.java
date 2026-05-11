package tn.esprit.pidev.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer sequence;
    private Double latitude;
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "line_id", nullable = false)
    @JsonBackReference("line-stops")
    private Line line;
}
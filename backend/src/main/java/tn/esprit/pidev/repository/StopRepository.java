package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {
    List<Stop> findByLineIdOrderBySequenceAsc(Long lineId);
    List<Stop> findByLineId(Long lineId);
}

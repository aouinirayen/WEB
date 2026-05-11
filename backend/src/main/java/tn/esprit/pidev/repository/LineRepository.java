package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Line;
import tn.esprit.pidev.entity.enums.LineStatus;
import tn.esprit.pidev.entity.enums.TransportMode;
import java.util.List;

@Repository
public interface LineRepository
        extends JpaRepository<Line, Long> {

    List<Line> findByStatus(LineStatus status);
    List<Line> findByMode(TransportMode mode);
}
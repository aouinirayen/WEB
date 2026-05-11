package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.pidev.entity.Incident;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

}

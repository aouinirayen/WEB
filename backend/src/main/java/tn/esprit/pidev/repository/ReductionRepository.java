package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Reduction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReductionRepository extends JpaRepository<Reduction, Long> {
    Optional<Reduction> findByCode(String code);
    List<Reduction> findByDateExpirationAfter(LocalDate date);
    List<Reduction> findByPointsRequisLessThanEqual(Integer points);
    List<Reduction> findByCreatedById(Long operatorId);
}

package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.PricingPlan;
import tn.esprit.pidev.entity.PricingType;

import java.util.List;

@Repository
public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long> {
    List<PricingPlan> findByType(PricingType type);
    List<PricingPlan> findByPrixLessThanEqual(Double prix);
    List<PricingPlan> findByCreatedById(Long operatorId);
}

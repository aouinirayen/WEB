package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.PricingPlanRequest;
import tn.esprit.pidev.dto.PricingPlanResponse;
import tn.esprit.pidev.entity.PricingType;

import java.util.List;

public interface IPricingPlanService {
    PricingPlanResponse create(PricingPlanRequest request, Long operatorId);
    PricingPlanResponse getById(Long id);
    List<PricingPlanResponse> getAll();
    PricingPlanResponse update(Long id, PricingPlanRequest request, Long operatorId);
    void delete(Long id, Long operatorId);
    List<PricingPlanResponse> getByType(PricingType type);
    List<PricingPlanResponse> getByMaxPrice(Double maxPrix);
    List<PricingPlanResponse> getByOperator(Long operatorId);
}

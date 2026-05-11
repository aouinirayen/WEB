package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.ReductionRequest;
import tn.esprit.pidev.dto.ReductionResponse;

import java.util.List;

public interface IReductionService {
    ReductionResponse create(ReductionRequest request, Long operatorId);
    ReductionResponse getById(Long id);
    ReductionResponse getByCode(String code);
    List<ReductionResponse> getAll();
    ReductionResponse update(Long id, ReductionRequest request);
    void delete(Long id);
    List<ReductionResponse> getValides();
    List<ReductionResponse> getAccessibles(Integer points);
    List<ReductionResponse> getByOperator(Long operatorId);
}

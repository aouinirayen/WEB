package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.ActionSendResponse;
import tn.esprit.pidev.dto.ChurnPredictionResponse;
import tn.esprit.pidev.dto.CLVResponse;
import tn.esprit.pidev.dto.PlanRecommendationResponse;

import java.util.List;

public interface IMLService {
    PlanRecommendationResponse    recommendPlan(Long passengerId);
    ChurnPredictionResponse       predictChurn(Long passengerId);
    List<ChurnPredictionResponse> predictChurnAll();
    CLVResponse                   predictCLV(Long passengerId);
    ActionSendResponse            sendAction(Long passengerId);
}
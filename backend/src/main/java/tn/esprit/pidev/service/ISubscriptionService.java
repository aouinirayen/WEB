package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.SubscriptionRequest;
import tn.esprit.pidev.dto.SubscriptionResponse;
import tn.esprit.pidev.entity.SubscriptionStatus;

import java.util.List;

public interface ISubscriptionService {
    SubscriptionResponse subscribe(SubscriptionRequest request, Long passengerId);
    SubscriptionResponse getById(Long id);
    List<SubscriptionResponse> getAll();
    List<SubscriptionResponse> getByOperator(Long operatorId);
    List<SubscriptionResponse> getByPassenger(Long passengerId);
    List<SubscriptionResponse> getByStatut(SubscriptionStatus statut);
    SubscriptionResponse cancel(Long id, Long passengerId);
    SubscriptionResponse updateAutoRenewal(Long id, Long passengerId, boolean autoRenewal);
    void delete(Long id);
}

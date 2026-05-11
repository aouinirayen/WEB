package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.LoyaltyAccountResponse;
import tn.esprit.pidev.dto.RedeemRequest;
import tn.esprit.pidev.entity.LoyaltyTier;

import java.util.List;

public interface ILoyaltyAccountService {
    LoyaltyAccountResponse getById(Long id);
    LoyaltyAccountResponse getByPassenger(Long passengerId);
    List<LoyaltyAccountResponse> getAll();
    List<LoyaltyAccountResponse> getByTier(LoyaltyTier tier);
    LoyaltyAccountResponse redeemPoints(Long passengerId, RedeemRequest request);
    void delete(Long id);
}

package tn.esprit.pidev.service;

import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.LoyaltyAccountResponse;
import tn.esprit.pidev.dto.RedeemRequest;
import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.LoyaltyAccountRepository;
import tn.esprit.pidev.repository.PointTransactionRepository;
import tn.esprit.pidev.repository.ReductionRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoyaltyAccountServiceImpl implements ILoyaltyAccountService {

    private final LoyaltyAccountRepository loyaltyRepo;
    private final PointTransactionRepository txRepo;
    private final ReductionRepository reductionRepo;
    private final UserRepository userRepo;

    public LoyaltyAccountServiceImpl(LoyaltyAccountRepository loyaltyRepo,
                                     PointTransactionRepository txRepo,
                                     ReductionRepository reductionRepo,
                                     UserRepository userRepo) {
        this.loyaltyRepo = loyaltyRepo;
        this.txRepo = txRepo;
        this.reductionRepo = reductionRepo;
        this.userRepo = userRepo;
    }

    @Override
    public LoyaltyAccountResponse getById(Long id) {
        return LoyaltyAccountResponse.fromEntity(findById(id));
    }

    @Override
    public LoyaltyAccountResponse getByPassenger(Long passengerId) {
        LoyaltyAccount la = loyaltyRepo.findByPassengerId(passengerId)
                .orElseGet(() -> {
                    User passenger = userRepo.findById(passengerId)
                            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + passengerId));
                    return loyaltyRepo.save(new LoyaltyAccount(passenger));
                });
        return LoyaltyAccountResponse.fromEntity(la);
    }

    @Override
    public List<LoyaltyAccountResponse> getAll() {
        return loyaltyRepo.findAll().stream()
                .map(LoyaltyAccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoyaltyAccountResponse> getByTier(LoyaltyTier tier) {
        return loyaltyRepo.findByNiveau(tier).stream()
                .map(LoyaltyAccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public LoyaltyAccountResponse redeemPoints(Long passengerId, RedeemRequest request) {
        LoyaltyAccount la = loyaltyRepo.findByPassengerId(passengerId)
                .orElseThrow(() -> new RuntimeException("Compte fidélité introuvable."));

        Reduction red = reductionRepo.findById(request.getReductionId())
                .orElseThrow(() -> new RuntimeException("Réduction introuvable : " + request.getReductionId()));

        if (la.getPointsCumules() < red.getPointsRequis()) {
            throw new RuntimeException("Points insuffisants. Requis : "
                    + red.getPointsRequis() + ", disponible : " + la.getPointsCumules());
        }

        int pointsAUtiliser = request.getPointsAUtiliser();
        la.setPointsCumules(la.getPointsCumules() - pointsAUtiliser);
        la.setNiveau(calculerTier(la.getPointsCumules()));
        loyaltyRepo.save(la);

        txRepo.save(new PointTransaction(
                pointsAUtiliser, TransactionType.REDEEMED, LocalDateTime.now(),
                "Points utilisés pour la réduction : " + red.getCode(), la));

        return LoyaltyAccountResponse.fromEntity(la);
    }

    @Override
    public void delete(Long id) {
        findById(id);
        loyaltyRepo.deleteById(id);
    }

    private LoyaltyAccount findById(Long id) {
        return loyaltyRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("LoyaltyAccount introuvable : " + id));
    }

    private LoyaltyTier calculerTier(int pts) {
        if (pts >= 500) return LoyaltyTier.GOLD;
        if (pts >= 200) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }
}

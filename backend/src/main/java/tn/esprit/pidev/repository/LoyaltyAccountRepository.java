package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.LoyaltyAccount;
import tn.esprit.pidev.entity.LoyaltyTier;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByPassengerId(Long passengerId);
    List<LoyaltyAccount> findByNiveau(LoyaltyTier niveau);
}

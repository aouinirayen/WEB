package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.PointTransaction;
import tn.esprit.pidev.entity.TransactionType;

import java.util.List;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByLoyaltyAccountId(Long loyaltyAccountId);
    List<PointTransaction> findByLoyaltyAccountIdAndType(Long loyaltyAccountId, TransactionType type);
    List<PointTransaction> findByLoyaltyAccountPassengerId(Long passengerId);
}

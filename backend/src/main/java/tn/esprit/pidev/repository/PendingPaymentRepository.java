package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.PendingPayment;
import java.util.Optional;

@Repository
public interface PendingPaymentRepository extends JpaRepository<PendingPayment, Long> {
    Optional<PendingPayment> findByStripeSessionId(String stripeSessionId);
}
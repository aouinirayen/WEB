package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.Partner;
import tn.esprit.pidev.enums.PartnerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    List<Partner> findByStatus(PartnerStatus status);
    boolean existsByEmail(String email);
}

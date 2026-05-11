package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartnerContractRepository extends JpaRepository<PartnerContract, Long> {

    List<PartnerContract> findByOrganizationId(Long organizationId);
    List<PartnerContract> findByPartnerId(Long partnerId);
    List<PartnerContract> findByStatus(ContractStatus status);
}

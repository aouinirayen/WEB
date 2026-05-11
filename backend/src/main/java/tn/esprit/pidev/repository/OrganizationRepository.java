package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.enums.OrgStatus;
import tn.esprit.pidev.enums.CoverageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByStatus(OrgStatus status);
    List<Organization> findByCoverageType(CoverageType coverageType);
    boolean existsByName(String name);
}

package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.OperatorZone;
import tn.esprit.pidev.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OperatorZoneRepository extends JpaRepository<OperatorZone, Long> {

    List<OperatorZone> findByOrganizationId(Long organizationId);
    List<OperatorZone> findByRegion(Region region);
}

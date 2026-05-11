package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.VehiclePosition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehiclePositionRepository extends JpaRepository<VehiclePosition, Long> {
    Optional<VehiclePosition> findByVehicleId(Long vehicleId);
}

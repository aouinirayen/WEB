package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Vehicle;
import tn.esprit.pidev.entity.enums.VehicleStatus;
import tn.esprit.pidev.entity.enums.VehicleType;
import java.util.List;

@Repository
public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByStatus(VehicleStatus status);
    List<Vehicle> findByType(VehicleType type);
}
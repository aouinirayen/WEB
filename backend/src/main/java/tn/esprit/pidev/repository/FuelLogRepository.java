package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface FuelLogRepository extends JpaRepository<FuelLog, Long> {

    List<FuelLog> findByVehicleId(Long vehicleId);

    List<FuelLog> findByFuelDateBetween(String from, String to);

    /** Total fuel cost across all vehicles */
    @Query("SELECT SUM(f.totalCost) FROM FuelLog f")
    Double sumTotalCost();

    /** Total liters across all vehicles */
    @Query("SELECT SUM(f.liters) FROM FuelLog f")
    Double sumLiters();

    /** Average cost per liter across all logs */
    @Query("SELECT AVG(f.costPerLiter) FROM FuelLog f")
    Double avgCostPerLiter();
}

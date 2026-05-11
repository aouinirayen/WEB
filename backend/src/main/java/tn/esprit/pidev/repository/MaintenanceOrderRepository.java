package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.MaintenanceOrder;
import tn.esprit.pidev.entity.enums.MaintenanceStatus;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceOrderRepository
        extends JpaRepository<MaintenanceOrder, Long> {

    List<MaintenanceOrder> findByVehicleId(Long vehicleId);
    List<MaintenanceOrder> findByStatus(MaintenanceStatus status);

    @Query("SELECT m FROM MaintenanceOrder m WHERE " +
            "m.scheduledDate BETWEEN :today AND :nextWeek " +
            "AND m.status = 'PENDING'")
    List<MaintenanceOrder> findUpcoming(
            @Param("today") LocalDate today,
            @Param("nextWeek") LocalDate nextWeek
    );
}
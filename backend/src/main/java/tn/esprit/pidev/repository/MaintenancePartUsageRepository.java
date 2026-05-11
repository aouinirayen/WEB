package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.MaintenancePartUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MaintenancePartUsageRepository extends JpaRepository<MaintenancePartUsage, Long> {

    List<MaintenancePartUsage> findByMaintenanceOrderId(Long maintenanceOrderId);

    List<MaintenancePartUsage> findBySparePartId(Long sparePartId);

    /** Total parts cost for a given maintenance order */
    @Query("SELECT SUM(u.totalCost) FROM MaintenancePartUsage u WHERE u.maintenanceOrder.id = :orderId")
    Double sumCostByMaintenanceOrder(Long orderId);

    /** Total spend on a specific part across all orders */
    @Query("SELECT SUM(u.totalCost) FROM MaintenancePartUsage u WHERE u.sparePart.id = :partId")
    Double sumCostByPart(Long partId);
}

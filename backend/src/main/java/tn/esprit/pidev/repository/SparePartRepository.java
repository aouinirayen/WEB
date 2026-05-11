package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.SparePart;
import tn.esprit.pidev.entity.SparePart.PartCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    boolean existsByReferenceCode(String referenceCode);

    List<SparePart> findByCategory(PartCategory category);

    /** All parts where stock is at or below the minimum threshold */
    @Query("SELECT s FROM SparePart s WHERE s.stockQuantity <= s.minStockThreshold")
    List<SparePart> findLowStockParts();

    /** Parts commonly used for a given category — used for auto-suggest */
    List<SparePart> findByCategoryIn(List<PartCategory> categories);
}

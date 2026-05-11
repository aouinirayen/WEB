package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.DriverRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DriverRatingRepository extends JpaRepository<DriverRating, Long> {

    List<DriverRating> findByDriverName(String driverName);

    @Query("SELECT dr.driverName, AVG(dr.predictedScore), COUNT(dr) FROM DriverRating dr GROUP BY dr.driverName ORDER BY AVG(dr.predictedScore) DESC")
    List<Object[]> getDriverAverages();
}

package tn.esprit.pidev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.pidev.entity.Driver;
import tn.esprit.pidev.entity.enums.DriverStatus;
import tn.esprit.pidev.entity.enums.LicenseType;
import tn.esprit.pidev.entity.enums.LicenseValidationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository
        extends JpaRepository<Driver, Long> {

    List<Driver> findByStatus(DriverStatus status);
    List<Driver> findByValidationStatus(LicenseValidationStatus status);

    // Find best driver for auto-assignment
    @Query("SELECT d FROM Driver d WHERE " +
            "d.licenseType = :licenseType AND " +
            "d.status = 'AVAILABLE' AND " +
            "d.validationStatus = 'APPROVED' AND " +
            "d.licenseExpiryDate > :today " +
            "ORDER BY d.experienceYears DESC")
    List<Driver> findBestAvailableDrivers(
            @Param("licenseType") LicenseType licenseType,
            @Param("today") LocalDate today
    );

    Optional<Driver> findByAssignedVehicleId(Long vehicleId);
}
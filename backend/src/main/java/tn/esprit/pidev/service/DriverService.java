package tn.esprit.pidev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.Driver;
import tn.esprit.pidev.entity.Vehicle;
import tn.esprit.pidev.entity.enums.*;
import tn.esprit.pidev.entity.enums.*;
import tn.esprit.pidev.repository.DriverRepository;
import tn.esprit.pidev.repository.VehicleRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    public List<Driver> getAll() {
        return driverRepository.findAll();
    }

    public Optional<Driver> getById(Long id) {
        return driverRepository.findById(id);
    }

    public Driver create(Driver driver) {
        // New drivers start with PENDING validation
        driver.setValidationStatus(LicenseValidationStatus.PENDING);
        driver.setStatus(DriverStatus.AVAILABLE);
        return driverRepository.save(driver);
    }

    public Driver update(Long id, Driver driver) {
        driver.setId(id);
        return driverRepository.save(driver);
    }

    public void delete(Long id) {
        driverRepository.deleteById(id);
    }

    public List<Driver> getPendingValidations() {
        return driverRepository.findByValidationStatus(
                LicenseValidationStatus.PENDING);
    }

    // Métier Avancé 2 — Validate license
    public Driver approveLicense(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setValidationStatus(LicenseValidationStatus.APPROVED);
        driver.setRejectionReason(null);
        return driverRepository.save(driver);
    }

    public Driver rejectLicense(Long id, String reason) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setValidationStatus(LicenseValidationStatus.REJECTED);
        driver.setRejectionReason(reason);
        driver.setStatus(DriverStatus.SUSPENDED);
        return driverRepository.save(driver);
    }

    // Métier Avancé 1 — Auto assign driver to vehicle
    public Driver autoAssignDriver(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Map vehicle type to required license type
        LicenseType requiredLicense = mapVehicleTypeToLicense(
                vehicle.getType());

        // Find best available driver
        List<Driver> candidates = driverRepository
                .findBestAvailableDrivers(
                        requiredLicense,
                        LocalDate.now()
                );

        if (candidates.isEmpty()) {
            throw new RuntimeException(
                    "No available driver found for vehicle type: "
                            + vehicle.getType());
        }

        // Take the most experienced one (first in list)
        Driver best = candidates.get(0);
        best.setAssignedVehicle(vehicle);
        best.setStatus(DriverStatus.ASSIGNED);

        // Update vehicle status
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicleRepository.save(vehicle);

        return driverRepository.save(best);
    }

    // Unassign driver from vehicle
    public Driver unassignDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setAssignedVehicle(null);
        driver.setStatus(DriverStatus.AVAILABLE);
        return driverRepository.save(driver);
    }

    // Map vehicle type to license type
    private LicenseType mapVehicleTypeToLicense(VehicleType type) {
        return switch (type) {
            case BUS -> LicenseType.C;
            case LOUAGE, BATAH -> LicenseType.D;
            case METRO, TRAIN -> LicenseType.TC;
            default -> LicenseType.B;
        };
    }
}
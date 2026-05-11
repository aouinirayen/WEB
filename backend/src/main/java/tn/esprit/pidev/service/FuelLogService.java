package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.FuelLog;
import tn.esprit.pidev.repository.FuelLogRepository;
import tn.esprit.pidev.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FuelLogService {

    private final FuelLogRepository fuelLogRepo;
    private final VehicleRepository vehicleRepo;

    public FuelLogService(FuelLogRepository fuelLogRepo, VehicleRepository vehicleRepo) {
        this.fuelLogRepo = fuelLogRepo;
        this.vehicleRepo = vehicleRepo;
    }

    public List<FuelLog> getAll() {
        return fuelLogRepo.findAll();
    }

    public FuelLog getById(Long id) {
        return fuelLogRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Fuel log not found: " + id));
    }

    public List<FuelLog> getByVehicle(Long vehicleId) {
        vehicleRepo.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));
        return fuelLogRepo.findByVehicleId(vehicleId);
    }

    public FuelLog create(FuelLog log) {
        recalcTotal(log);
        return fuelLogRepo.save(log);
    }

    public FuelLog update(Long id, FuelLog updated) {
        FuelLog existing = getById(id);
        existing.setVehicle(updated.getVehicle());
        existing.setLiters(updated.getLiters());
        existing.setCostPerLiter(updated.getCostPerLiter());
        existing.setMileageAtFillUp(updated.getMileageAtFillUp());
        existing.setFuelDate(updated.getFuelDate());
        existing.setStation(updated.getStation());
        existing.setNotes(updated.getNotes());
        recalcTotal(existing);
        return fuelLogRepo.save(existing);
    }

    public void delete(Long id) {
        fuelLogRepo.deleteById(id);
    }

    /** Always recompute totalCost server-side — never trust client value */
    private void recalcTotal(FuelLog log) {
        if (log.getLiters() != null && log.getCostPerLiter() != null) {
            double raw = log.getLiters() * log.getCostPerLiter();
            log.setTotalCost(Math.round(raw * 1000.0) / 1000.0);
        }
    }
}

package tn.esprit.pidev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.entity.Vehicle;
import tn.esprit.pidev.entity.enums.VehicleStatus;
import tn.esprit.pidev.entity.enums.VehicleType;
import tn.esprit.pidev.repository.VehicleRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository repository;

    public List<Vehicle> getAll() {
        return repository.findAll();
    }

    public Optional<Vehicle> getById(Long id) {
        return repository.findById(id);
    }

    public Vehicle create(Vehicle vehicle) {
        return repository.save(vehicle);
    }

    public Vehicle update(Long id, Vehicle vehicle) {
        vehicle.setId(id);
        return repository.save(vehicle);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Vehicle> getByStatus(VehicleStatus status) {
        return repository.findByStatus(status);
    }

    public List<Vehicle> getByType(VehicleType type) {
        return repository.findByType(type);
    }
}
package tn.esprit.pidev.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.service.GlobalNotificationService;
import tn.esprit.pidev.entity.MaintenanceOrder;
import tn.esprit.pidev.entity.enums.MaintenanceStatus;
import tn.esprit.pidev.repository.MaintenanceOrderRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceOrderRepository repository;
    private final GlobalNotificationService globalNotif;

    public List<MaintenanceOrder> getAll() {
        return repository.findAll();
    }

    public Optional<MaintenanceOrder> getById(Long id) {
        return repository.findById(id);
    }

    public MaintenanceOrder create(MaintenanceOrder order) {
        MaintenanceOrder saved = repository.save(order);
        globalNotif.notifyAllAdmins("Maintenance programmee",
            "Ordre #" + saved.getId() + " - " + saved.getDescription()
            + " | Vehicule: " + (saved.getVehicle() != null ? saved.getVehicle().getPlateNumber() : "N/A")
            + " | Type: " + saved.getType());
        return saved;
    }

    public MaintenanceOrder update(Long id, MaintenanceOrder order) {
        order.setId(id);
        return repository.save(order);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<MaintenanceOrder> getByVehicle(Long vehicleId) {
        return repository.findByVehicleId(vehicleId);
    }

    public List<MaintenanceOrder> getByStatus(MaintenanceStatus status) {
        return repository.findByStatus(status);
    }

    public List<MaintenanceOrder> getUpcomingAlerts() {
        return repository.findUpcoming(
                LocalDate.now(),
                LocalDate.now().plusDays(7)
        );
    }
}
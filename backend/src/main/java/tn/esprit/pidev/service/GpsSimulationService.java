package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.Stop;
import tn.esprit.pidev.entity.Vehicle;
import tn.esprit.pidev.entity.VehiclePosition;
import tn.esprit.pidev.repository.StopRepository;
import tn.esprit.pidev.repository.VehicleRepository;
import tn.esprit.pidev.repository.VehiclePositionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * GPS Simulation Service
 *
 * - Every 10 seconds, advances all simulated vehicles one stop forward
 *   along their assigned line's stops.
 * - Real GPS positions are updated via the REST endpoint (from the browser).
 * - Positions are stored in vehicle_positions table (one row per vehicle).
 */
@Service
public class GpsSimulationService {

    private final VehicleRepository         vehicleRepo;
    private final VehiclePositionRepository positionRepo;
    private final StopRepository            stopRepo;
    private final Random random = new Random();

    public GpsSimulationService(VehicleRepository vehicleRepo,
                                 VehiclePositionRepository positionRepo,
                                 StopRepository stopRepo) {
        this.vehicleRepo  = vehicleRepo;
        this.positionRepo = positionRepo;
        this.stopRepo     = stopRepo;
    }

    /** Called every 10 seconds — advances simulated vehicles */
    @Scheduled(fixedDelay = 10000)
    public void simulateMovement() {
        List<Vehicle> activeVehicles = vehicleRepo.findAll().stream()
                .filter(v -> v.getStatus() != null && v.getStatus().name().equals("ACTIVE"))
            .toList();

        for (Vehicle vehicle : activeVehicles) {
            VehiclePosition pos = positionRepo.findByVehicleId(vehicle.getId())
                .orElse(null);

            // Skip vehicles with real GPS (not simulated)
            if (pos != null && !pos.isSimulated()) continue;

            // Get stops for this vehicle's line (via active schedule)
            List<Stop> stops = stopRepo.findAll().stream()
                .filter(s -> s.getLine() != null)
                .sorted((a, b) -> a.getSequence() - b.getSequence())
                .toList();

            if (stops.isEmpty()) continue;

            if (pos == null) {
                pos = new VehiclePosition();
                pos.setVehicle(vehicle);
                pos.setCurrentStopIndex(0);
                pos.setSimulated(true);
            }

            // Advance to next stop (wrap around)
            int nextIdx = ((pos.getCurrentStopIndex() == null ? 0 : pos.getCurrentStopIndex()) + 1)
                          % stops.size();
            Stop nextStop = stops.get(nextIdx);

            // Add small random jitter to make it look realistic
            double jitterLat = (random.nextDouble() - 0.5) * 0.001;
            double jitterLon = (random.nextDouble() - 0.5) * 0.001;

            pos.setLatitude(nextStop.getLatitude() + jitterLat);
            pos.setLongitude(nextStop.getLongitude() + jitterLon);
            pos.setCurrentStopIndex(nextIdx);
            pos.setSpeedKmh(30.0 + random.nextDouble() * 20);
            pos.setHeading(headingFor(nextIdx));
            pos.setStatus("ON_ROUTE");
            pos.setUpdatedAt(LocalDateTime.now().toString());

            positionRepo.save(pos);
        }
    }

    /** Update position from real browser GPS */
    public VehiclePosition updateRealGps(Long vehicleId, double lat, double lon) {
        Vehicle vehicle = vehicleRepo.findById(vehicleId)
            .orElseThrow(() -> new RuntimeException("Vehicle not found: " + vehicleId));

        VehiclePosition pos = positionRepo.findByVehicleId(vehicleId)
            .orElse(new VehiclePosition());

        pos.setVehicle(vehicle);
        pos.setLatitude(lat);
        pos.setLongitude(lon);
        pos.setSimulated(false);
        pos.setStatus("ON_ROUTE");
        pos.setSpeedKmh(0.0);
        pos.setUpdatedAt(LocalDateTime.now().toString());
        return positionRepo.save(pos);
    }

    public List<VehiclePosition> getAllPositions() {
        return positionRepo.findAll();
    }

    public VehiclePosition getPosition(Long vehicleId) {
        return positionRepo.findByVehicleId(vehicleId)
            .orElseThrow(() -> new RuntimeException("No position for vehicle: " + vehicleId));
    }

    private String headingFor(int idx) {
        String[] dirs = { "NORTH", "NORTHEAST", "EAST", "SOUTHEAST",
                          "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST" };
        return dirs[idx % dirs.length];
    }
}

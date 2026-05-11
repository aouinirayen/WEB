package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.RouteResultDTO;
import tn.esprit.pidev.entity.VehiclePosition;
import tn.esprit.pidev.service.GpsSimulationService;
import tn.esprit.pidev.service.RouteOptimizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RouteOptimizationController {

    private final RouteOptimizationService routeService;
    private final GpsSimulationService     gpsService;

    public RouteOptimizationController(RouteOptimizationService routeService,
                                        GpsSimulationService gpsService) {
        this.routeService = routeService;
        this.gpsService   = gpsService;
    }

    // ── Route Optimization ────────────────────────────────────────

    /**
     * GET /api/routes/optimize/{lineId}
     * Returns optimal route for a line using Dijkstra or Weighted AI.
     */
    @GetMapping("/routes/optimize/{lineId}")
    public RouteResultDTO optimizeRoute(@PathVariable Long lineId) {
        return routeService.optimize(lineId);
    }

    // ── GPS / Live Tracking ───────────────────────────────────────

    /**
     * GET /api/gps/positions
     * Returns all vehicle positions (simulated + real).
     */
    @GetMapping("/gps/positions")
    public List<VehiclePosition> getAllPositions() {
        return gpsService.getAllPositions();
    }

    /**
     * GET /api/gps/positions/{vehicleId}
     * Returns position for a specific vehicle.
     */
    @GetMapping("/gps/positions/{vehicleId}")
    public VehiclePosition getPosition(@PathVariable Long vehicleId) {
        return gpsService.getPosition(vehicleId);
    }

    /**
     * POST /api/gps/positions/{vehicleId}
     * Updates real GPS position from browser (body: { lat, lon })
     */
    @PostMapping("/gps/positions/{vehicleId}")
    public ResponseEntity<VehiclePosition> updateRealGps(
            @PathVariable Long vehicleId,
            @RequestBody Map<String, Double> body) {
        double lat = body.get("lat");
        double lon = body.get("lon");
        return ResponseEntity.ok(gpsService.updateRealGps(vehicleId, lat, lon));
    }
}

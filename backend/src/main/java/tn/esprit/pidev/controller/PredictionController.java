package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.PartPredictionDTO;
import tn.esprit.pidev.service.PredictionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService service;

    public PredictionController(PredictionService service) {
        this.service = service;
    }

    /**
     * GET /api/predictions/fleet
     * Returns predictions for all parts across the entire fleet,
     * sorted by urgency (most urgent first).
     */
    @GetMapping("/fleet")
    public List<PartPredictionDTO> fleetPredictions() {
        return service.predictFleet();
    }

    /**
     * GET /api/predictions/vehicle/{vehicleId}
     * Returns predictions scoped to a specific vehicle's maintenance history.
     */
    @GetMapping("/vehicle/{vehicleId}")
    public List<PartPredictionDTO> vehiclePredictions(@PathVariable Long vehicleId) {
        return service.predictForVehicle(vehicleId);
    }
}

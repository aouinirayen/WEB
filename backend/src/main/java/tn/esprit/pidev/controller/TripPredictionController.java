package tn.esprit.pidev.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.service.TripPredictionService;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class TripPredictionController {

    private final TripPredictionService service;

    @GetMapping("/trips")
    public Map<String, Object> predictTrips() {
        return service.predictDemand();
    }
}

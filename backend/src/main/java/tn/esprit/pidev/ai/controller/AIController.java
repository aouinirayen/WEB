package tn.esprit.pidev.ai.controller;

import tn.esprit.pidev.ai.dto.*;
import tn.esprit.pidev.ai.service.AITrainingService;
import tn.esprit.pidev.ai.service.CancellationPredictionService;
import tn.esprit.pidev.ai.service.MatchingService;
import tn.esprit.pidev.ai.service.SatisfactionPredictionService;
import tn.esprit.pidev.entity.Covoiturage;
import tn.esprit.pidev.entity.DriverRating;
import tn.esprit.pidev.repository.CovoiturageRepository;
import tn.esprit.pidev.repository.DriverRatingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AIController {

    private final MatchingService matchingService;
    private final CancellationPredictionService cancellationService;
    private final SatisfactionPredictionService satisfactionService;
    private final AITrainingService aiTrainingService;
    private final CovoiturageRepository covoiturageRepository;
    private final DriverRatingRepository driverRatingRepository;

    public AIController(MatchingService matchingService,
                         CancellationPredictionService cancellationService,
                         SatisfactionPredictionService satisfactionService,
                         AITrainingService aiTrainingService,
                         CovoiturageRepository covoiturageRepository,
                         DriverRatingRepository driverRatingRepository) {
        this.matchingService = matchingService;
        this.cancellationService = cancellationService;
        this.satisfactionService = satisfactionService;
        this.aiTrainingService = aiTrainingService;
        this.covoiturageRepository = covoiturageRepository;
        this.driverRatingRepository = driverRatingRepository;
    }

    @PostMapping("/matching")
    public ResponseEntity<?> findMatches(@RequestBody MatchRequest request) {
        if (!matchingService.isReady()) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Matching model not ready",
                    "message", "Models are still training. Please retry in a few seconds."
            ));
        }
        List<MatchResult> results = matchingService.findBestMatches(
                request.getPassengerLat(), request.getPassengerLng(),
                request.getDestLat(), request.getDestLng(),
                request.getHeureVoulue(), request.getBudgetMax()
        );
        return ResponseEntity.ok(results);
    }

    @PostMapping("/cancellation")
    public ResponseEntity<?> predictCancellation(@RequestBody CancellationRequest request) {
        if (!cancellationService.isReady()) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Cancellation model not ready",
                    "message", "Models are still training. Please retry in a few seconds."
            ));
        }
        CancellationResponse response = cancellationService.predictCancellation(
                request.getPrix(), request.getDistanceKm(),
                request.getJoursAvant(), request.getHeure(), request.getNbPlaces()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/satisfaction")
    public ResponseEntity<?> predictSatisfaction(@RequestBody SatisfactionRequest request) {
        if (!satisfactionService.isReady()) {
            return ResponseEntity.status(503).body(Map.of(
                    "error", "Satisfaction model not ready",
                    "message", "Models are still training. Please retry in a few seconds."
            ));
        }
        SatisfactionResponse response = satisfactionService.predictSatisfaction(
                request.getCovoiturageId(),
                request.getMatchScore(), request.getPrixRatio(),
                request.getPonctualite(), request.getPlacesRatio(), request.getDetourKm()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/retrain")
    public ResponseEntity<?> retrain() {
        try {
            aiTrainingService.retrainModels();
            return ResponseEntity.ok(Map.of(
                    "message", "All 3 AI models retrained successfully",
                    "stats", aiTrainingService.getStats()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Retraining failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<AIStatsResponse> getStats() {
        return ResponseEntity.ok(aiTrainingService.getStats());
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", aiTrainingService.isReady() ? "READY" : "NOT_READY",
                "matching", matchingService.isReady(),
                "cancellation", cancellationService.isReady(),
                "satisfaction", satisfactionService.isReady()
        ));
    }

    @GetMapping("/covoiturages-list")
    public ResponseEntity<?> listCovoiturages() {
        List<Covoiturage> all = covoiturageRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Covoiturage c : all) {
            // Seuls les covoiturages termines peuvent etre evalues
            if (!"COMPLETED".equalsIgnoreCase(c.getStatus())) continue;
            result.add(Map.of(
                "id", c.getId(),
                "label", c.getDriverName() + " — " + c.getDeparture() + " → " + c.getDestination() + " (" + c.getDate() + ")"
            ));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/driver-ratings")
    public ResponseEntity<?> getDriverRatings() {
        List<Object[]> avgs = driverRatingRepository.getDriverAverages();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : avgs) {
            result.add(Map.of(
                "driverName", row[0],
                "avgScore", Math.round(((Number) row[1]).doubleValue() * 100.0) / 100.0,
                "totalRatings", ((Number) row[2]).intValue()
            ));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/seed-test-data")
    public ResponseEntity<?> seedTestData() {
        List<Covoiturage> created = new ArrayList<>();
        String[][] data = {
            {"Ahmed Ben Ali","Tunis","Sousse","08:00","10:00","15","3","Peugeot 308","36.8065","10.1815","35.8245","10.6346"},
            {"Fatma Trabelsi","Tunis","Nabeul","07:30","08:45","10","2","Renault Clio","36.8065","10.1815","36.4513","10.7357"},
            {"Mohamed Jaziri","Sousse","Sfax","09:00","11:00","12","4","Volkswagen Golf","35.8245","10.6346","34.7398","10.7600"},
            {"Sana Hamdi","Tunis","Bizerte","06:30","07:45","8","1","Citroen C3","36.8065","10.1815","37.2744","9.8739"},
            {"Karim Mejri","Ariana","Sousse","08:30","10:30","14","3","Hyundai i20","36.8625","10.1956","35.8245","10.6346"},
            {"Ines Bouslama","La Marsa","Hammamet","10:00","11:15","11","2","Dacia Sandero","36.8764","10.3253","36.4000","10.6167"},
            {"Youssef Khelifi","Tunis","Kairouan","07:00","09:30","18","3","Toyota Yaris","36.8065","10.1815","35.6781","10.0963"},
            {"Amira Gharbi","Ben Arous","Sfax","06:00","09:30","25","4","Kia Picanto","36.7533","10.2283","34.7398","10.7600"},
            {"Nabil Sassi","Manouba","Zaghouan","09:00","10:00","7","2","Fiat Punto","36.8101","10.0956","36.4029","10.1429"},
            {"Rim Ayari","Nabeul","Tunis","17:00","18:15","10","3","Peugeot 208","36.4513","10.7357","36.8065","10.1815"}
        };
        for (String[] d : data) {
            Covoiturage c = new Covoiturage();
            c.setDriverName(d[0]); c.setDeparture(d[1]); c.setDestination(d[2]);
            c.setDate(LocalDate.now().plusDays(1));
            c.setHeureDepart(d[3]); c.setHeureArrivee(d[4]);
            c.setPrice(Double.parseDouble(d[5])); c.setAvailableSeats(Integer.parseInt(d[6]));
            c.setVehicle(d[7]); c.setStatus("CONFIRMED");
            c.setDepartureLat(Double.parseDouble(d[8])); c.setDepartureLng(Double.parseDouble(d[9]));
            c.setDestinationLat(Double.parseDouble(d[10])); c.setDestinationLng(Double.parseDouble(d[11]));
            created.add(covoiturageRepository.save(c));
        }
        return ResponseEntity.ok(Map.of("message", "10 covoiturages de test ajoutes", "count", created.size()));
    }

    @PostMapping("/seed-completed-data")
    public ResponseEntity<?> seedCompletedData() {
        List<Covoiturage> created = new ArrayList<>();
        // Donnees avec jours dans le passe (variees)
        Object[][] data = {
            {"Ahmed Ben Ali","Tunis","Sousse","08:00","10:00","15","0","Peugeot 308", 1},
            {"Fatma Trabelsi","Tunis","Nabeul","07:30","08:45","10","0","Renault Clio", 1},
            {"Mohamed Jaziri","Sousse","Sfax","09:00","11:00","12","0","Volkswagen Golf", 2},
            {"Karim Mejri","Ariana","Sousse","08:30","10:30","14","0","Hyundai i20", 2},
            {"Ines Bouslama","La Marsa","Hammamet","10:00","11:15","11","0","Dacia Sandero", 3},
            {"Youssef Khelifi","Tunis","Kairouan","07:00","09:30","18","0","Toyota Yaris", 3},
            {"Sana Hamdi","Tunis","Bizerte","06:30","07:45","8","0","Citroen C3", 5},
            {"Nabil Sassi","Manouba","Zaghouan","09:00","10:00","7","0","Fiat Punto", 5},
            {"Amira Gharbi","Ben Arous","Sfax","06:00","09:30","25","0","Kia Picanto", 7},
            {"Rim Ayari","Nabeul","Tunis","17:00","18:15","10","0","Peugeot 208", 7},
        };
        for (Object[] d : data) {
            Covoiturage c = new Covoiturage();
            c.setDriverName((String)d[0]); c.setDeparture((String)d[1]); c.setDestination((String)d[2]);
            c.setDate(LocalDate.now().minusDays((int)d[8]));
            c.setHeureDepart((String)d[3]); c.setHeureArrivee((String)d[4]);
            c.setPrice(Double.parseDouble((String)d[5])); c.setAvailableSeats(Integer.parseInt((String)d[6]));
            c.setVehicle((String)d[7]); c.setStatus("COMPLETED");
            created.add(covoiturageRepository.save(c));
        }
        return ResponseEntity.ok(Map.of("message", created.size() + " covoiturages termines ajoutes", "count", created.size()));
    }
}

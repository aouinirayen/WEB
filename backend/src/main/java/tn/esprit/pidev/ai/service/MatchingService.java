package tn.esprit.pidev.ai.service;

import tn.esprit.pidev.ai.dto.MatchResult;
import tn.esprit.pidev.ai.math.DataPreprocessor;
import tn.esprit.pidev.ai.math.HaversineCalculator;
import tn.esprit.pidev.ai.model.NeuralNetwork;
import tn.esprit.pidev.entity.Covoiturage;
import tn.esprit.pidev.repository.CovoiturageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final CovoiturageRepository covoiturageRepository;
    private NeuralNetwork neuralNetwork;
    private DataPreprocessor preprocessor;

    public MatchingService(CovoiturageRepository covoiturageRepository) {
        this.covoiturageRepository = covoiturageRepository;
    }

    public void setModel(NeuralNetwork nn, DataPreprocessor prep) {
        this.neuralNetwork = nn;
        this.preprocessor = prep;
    }

    public List<MatchResult> findBestMatches(double passengerLat, double passengerLng,
                                              double destLat, double destLng,
                                              String heureVoulue, double budgetMax) {
        if (neuralNetwork == null || preprocessor == null) {
            throw new IllegalStateException("Matching model not trained yet");
        }

        // Recuperer les VRAIS covoiturages filtres
        List<Covoiturage> covoiturages = covoiturageRepository.findAll();
        List<MatchResult> results = new ArrayList<>();
        LocalDate today = LocalDate.now();

        double heurePassager = parseHeure(heureVoulue);

        // Distance du trajet demande par le passager
        double tripDistance = HaversineCalculator.calculate(passengerLat, passengerLng, destLat, destLng);
        // Filtre dynamique : court trajet = filtre strict, long trajet = filtre large
        double destMaxDist = Math.max(20, tripDistance * 0.8);
        double depMaxDist = Math.max(30, tripDistance * 0.8);
        log.info("Matching: trip distance = {}km, destFilter = {}km, depFilter = {}km",
                String.format("%.1f", tripDistance), String.format("%.1f", destMaxDist), String.format("%.1f", depMaxDist));

        for (Covoiturage c : covoiturages) {
            // Filtrage : seats > 0, pas complete, date >= aujourd'hui
            if (c.getAvailableSeats() <= 0) continue;
            if ("COMPLETED".equalsIgnoreCase(c.getStatus()) || "CANCELLED".equalsIgnoreCase(c.getStatus())) continue;
            if (c.getDate() != null && c.getDate().isBefore(today)) continue;

            // Resoudre GPS depart — geocoder par nom de ville si null
            double depLat, depLng;
            if (c.getDepartureLat() != null && c.getDepartureLng() != null) {
                depLat = c.getDepartureLat();
                depLng = c.getDepartureLng();
            } else {
                double[] geo = HaversineCalculator.geocodeCity(c.getDeparture());
                if (geo == null) {
                    log.warn("Covoiturage {} skip: pas de GPS et ville '{}' inconnue", c.getId(), c.getDeparture());
                    continue;
                }
                depLat = geo[0];
                depLng = geo[1];
            }

            // Resoudre GPS destination — geocoder par nom de ville si null
            double cDestLat, cDestLng;
            if (c.getDestinationLat() != null && c.getDestinationLng() != null) {
                cDestLat = c.getDestinationLat();
                cDestLng = c.getDestinationLng();
            } else {
                double[] geo = HaversineCalculator.geocodeCity(c.getDestination());
                if (geo != null) {
                    cDestLat = geo[0];
                    cDestLng = geo[1];
                } else {
                    cDestLat = depLat;
                    cDestLng = depLng;
                }
            }

            // Feature 1 : distance passager → depart covoiturage
            double distToDep = HaversineCalculator.calculate(passengerLat, passengerLng, depLat, depLng);
            // Feature 2 : distance destination souhaitee → destination covoiturage
            double distToDest = HaversineCalculator.calculate(destLat, destLng, cDestLat, cDestLng);
            // Feature 3 : difference horaire
            double heureCovoit = parseHeure(c.getHeureDepart());
            double diffHeure = Math.abs(heurePassager - heureCovoit);
            // Feature 4 : difference de prix normalisee
            double diffPrix = budgetMax > 0 ? Math.abs(c.getPrice() - budgetMax) / budgetMax : 0;
            // Feature 5 : places disponibles
            double seats = (double) c.getAvailableSeats();
            // Feature 6 : jour de la semaine du covoiturage
            double jourSemaine = c.getDate() != null ? c.getDate().getDayOfWeek().getValue() : today.getDayOfWeek().getValue();
            // Feature 7 : cosinus angle entre direction passager et direction covoiturage
            double cosAngle = HaversineCalculator.cosineAngleBetweenVectors(
                    passengerLat, passengerLng, destLat, destLng,
                    depLat, depLng, cDestLat, cDestLng);

            // FILTRE DYNAMIQUE : proportionnel a la distance du trajet demande
            if (distToDest > destMaxDist) continue;
            if (distToDep > depMaxDist) continue;

            double[] features = {distToDep, distToDest, diffHeure, diffPrix, seats, jourSemaine, cosAngle};

            double[] normalized = preprocessor.transform(features);
            double[] prediction = neuralNetwork.predict(normalized);
            double nnScore = prediction[0];

            // --- Score logique DYNAMIQUE base sur la distance du trajet ---
            // Le denominateur s adapte au trajet : court trajet = scoring strict, long trajet = scoring souple
            double destRef = Math.max(20, tripDistance * 0.8);
            double depRef = Math.max(30, tripDistance * 0.8);

            // Destination proximity (MOST IMPORTANT)
            double destScore = Math.max(0, 1.0 - distToDest / destRef);
            // Departure proximity
            double depScore = Math.max(0, 1.0 - distToDep / depRef);
            // Direction alignment : cosAngle 1.0 = same direction, -1.0 = opposite
            double dirScore = Math.max(0, (cosAngle + 1.0) / 2.0);
            // Time fit : 0h diff = 1.0, 4h+ = 0.0
            double timeScore = Math.max(0, 1.0 - diffHeure / 4.0);
            // Price fit : within budget = 1.0, 2x budget = 0.0
            double priceScore = c.getPrice() <= budgetMax ? 1.0 : Math.max(0, 1.0 - (c.getPrice() - budgetMax) / budgetMax);

            // Score logique pondere : destination (50%) >> depart > direction > horaire > prix
            double logicScore = destScore * 0.50 + depScore * 0.20 + dirScore * 0.10 + timeScore * 0.12 + priceScore * 0.08;

            // Score final hybride : NN (30%) + Logique (70%)
            double finalScore = nnScore * 0.3 + logicScore * 0.7;

            // Construire le resultat avec TOUS les champs du vrai covoiturage
            MatchResult mr = new MatchResult();
            mr.setCovoiturageId(c.getId());
            mr.setDriverName(c.getDriverName());
            mr.setDeparture(c.getDeparture());
            mr.setDestination(c.getDestination());
            mr.setHeureDepart(c.getHeureDepart());
            mr.setHeureArrivee(c.getHeureArrivee());
            mr.setDate(c.getDate());
            mr.setPrice(c.getPrice());
            mr.setAvailableSeats(c.getAvailableSeats());
            mr.setVehicle(c.getVehicle());
            mr.setScore(Math.round(finalScore * 10000.0) / 10000.0);
            mr.setDistanceToDeparture(Math.round(distToDep * 100.0) / 100.0);
            mr.setDistanceToDestination(Math.round(distToDest * 100.0) / 100.0);

            if (finalScore >= 0.7) {
                mr.setRecommendation("Fortement recommande");
            } else if (finalScore >= 0.4) {
                mr.setRecommendation("Recommande");
            } else {
                mr.setRecommendation("Peu adapte");
            }

            results.add(mr);
        }

        results.sort(Comparator.comparingDouble(MatchResult::getScore).reversed());
        return results;
    }

    private double parseHeure(String heure) {
        if (heure == null || heure.isEmpty()) return 12.0;
        try {
            String[] parts = heure.replace("h", ":").replace("H", ":").split(":");
            double h = Double.parseDouble(parts[0].trim());
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                h += Double.parseDouble(parts[1].trim()) / 60.0;
            }
            return h;
        } catch (Exception e) {
            return 12.0;
        }
    }

    public boolean isReady() {
        return neuralNetwork != null && preprocessor != null;
    }
}

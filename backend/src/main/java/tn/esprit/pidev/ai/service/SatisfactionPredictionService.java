package tn.esprit.pidev.ai.service;

import tn.esprit.pidev.ai.dto.SatisfactionResponse;
import tn.esprit.pidev.ai.math.DataPreprocessor;
import tn.esprit.pidev.ai.model.LinearRegression;
import tn.esprit.pidev.entity.Covoiturage;
import tn.esprit.pidev.entity.DriverRating;
import tn.esprit.pidev.repository.CovoiturageRepository;
import tn.esprit.pidev.repository.DriverRatingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SatisfactionPredictionService {

    private static final Logger log = LoggerFactory.getLogger(SatisfactionPredictionService.class);

    private final CovoiturageRepository covoiturageRepository;
    private final DriverRatingRepository driverRatingRepository;
    private LinearRegression model;
    private DataPreprocessor preprocessor;

    public SatisfactionPredictionService(CovoiturageRepository covoiturageRepository,
                                          DriverRatingRepository driverRatingRepository) {
        this.covoiturageRepository = covoiturageRepository;
        this.driverRatingRepository = driverRatingRepository;
    }

    public void setModel(LinearRegression model, DataPreprocessor preprocessor) {
        this.model = model;
        this.preprocessor = preprocessor;
    }

    public SatisfactionResponse predictSatisfaction(Long covoiturageId, double matchScore, double prixRatio,
                                                      double ponctualite, double placesRatio,
                                                      double detourKm) {
        if (model == null || preprocessor == null) {
            throw new IllegalStateException("Satisfaction model not trained yet");
        }

        double[] features = {matchScore, prixRatio, ponctualite, placesRatio, detourKm};
        double[] normalized = preprocessor.transform(features);
        double mlScore = model.predict(normalized);

        // --- Score logique base sur les criteres reels ---
        // matchScore: 0.95 = excellent, 0.2 = mauvais
        // prixRatio: 0.5 = tres bon marche, 2.0 = trop cher (INVERSE : bas = mieux)
        // ponctualite: 1.0 = en avance, 0.1 = tres en retard
        // placesRatio: 1.0 = pleine, 0.1 = vide
        // detourKm: 0 = direct, 30 = grand detour
        double qualityScore = Math.min(1.0, matchScore / 0.95);
        double prixScore = Math.max(0, Math.min(1.0, 1.0 - (prixRatio - 0.5) / 1.5));
        double detourPenalty = Math.max(0, 1.0 - detourKm / 30.0);
        double logicScore = (qualityScore * 0.30 + prixScore * 0.20 + ponctualite * 0.25
                + placesRatio * 0.10 + detourPenalty * 0.15) * 5.0;
        logicScore = Math.max(1.0, Math.min(5.0, logicScore));

        // Score final : si logique quasi parfaite, l'utiliser directement
        double predictedScore;
        if (logicScore >= 4.5) {
            predictedScore = logicScore;
        } else if (logicScore <= 1.5) {
            predictedScore = logicScore;
        } else {
            predictedScore = mlScore * 0.10 + logicScore * 0.90;
        }
        predictedScore = Math.max(1.0, Math.min(5.0, predictedScore));

        int stars = model.getStarRating(predictedScore);

        String message;
        if (stars >= 4) {
            message = "Excellente experience prevue! Le passager devrait etre tres satisfait.";
        } else if (stars >= 3) {
            message = "Bonne experience prevue. Quelques ameliorations possibles.";
        } else {
            message = "Experience mitigee prevue. Des ameliorations significatives sont recommandees.";
        }

        SatisfactionResponse response = new SatisfactionResponse(
                Math.round(predictedScore * 100.0) / 100.0,
                stars,
                message
        );

        // Lookup covoiturage and save rating
        if (covoiturageId != null) {
            Covoiturage cov = covoiturageRepository.findById(covoiturageId).orElse(null);
            if (cov != null) {
                response.setDriverName(cov.getDriverName());
                response.setRoute(cov.getDeparture() + " → " + cov.getDestination());

                // Sauvegarder la note du conducteur
                DriverRating rating = new DriverRating();
                rating.setDriverName(cov.getDriverName());
                rating.setCovoiturageId(covoiturageId);
                rating.setRoute(cov.getDeparture() + " → " + cov.getDestination());
                rating.setPredictedScore(response.getPredictedScore());
                rating.setStars(stars);
                driverRatingRepository.save(rating);
                log.info("Rating saved: driver={}, stars={}, score={}", cov.getDriverName(), stars, predictedScore);

                // Calculer la moyenne du conducteur
                List<DriverRating> allRatings = driverRatingRepository.findByDriverName(cov.getDriverName());
                double avg = allRatings.stream().mapToDouble(DriverRating::getPredictedScore).average().orElse(0);
                response.setDriverAvgScore(Math.round(avg * 100.0) / 100.0);
                response.setDriverTotalRatings(allRatings.size());
            }
        }

        return response;
    }

    public boolean isReady() {
        return model != null && preprocessor != null;
    }
}

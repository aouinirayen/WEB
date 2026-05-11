package tn.esprit.pidev.ai.service;

import tn.esprit.pidev.ai.dto.CancellationResponse;
import tn.esprit.pidev.ai.math.DataPreprocessor;
import tn.esprit.pidev.ai.model.LogisticRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CancellationPredictionService {

    private static final Logger log = LoggerFactory.getLogger(CancellationPredictionService.class);

    private LogisticRegression model;
    private DataPreprocessor preprocessor;

    public void setModel(LogisticRegression model, DataPreprocessor preprocessor) {
        this.model = model;
        this.preprocessor = preprocessor;
    }

    public CancellationResponse predictCancellation(double prix, double distanceKm,
                                                      int joursAvant, double heure, int nbPlaces) {
        if (model == null || preprocessor == null) {
            throw new IllegalStateException("Cancellation model not trained yet");
        }

        double[] features = {prix, distanceKm, (double) joursAvant, heure, (double) nbPlaces};
        double[] normalized = preprocessor.transform(features);
        double lrProba = model.predict(normalized);

        // --- Score logique base sur des regles metier ---

        // Heure : 0h-5h = tres dangereux, 22h-23h = dangereux, 7h-20h = normal
        double heureRisk;
        if (heure >= 0 && heure < 6) {
            heureRisk = 0.9 + (5 - Math.min(heure, 5)) * 0.02; // 0h→1.0, 2h→0.96, 5h→0.90
        } else if (heure >= 22) {
            heureRisk = 0.7 + (heure - 22) * 0.1;
        } else if (heure >= 7 && heure <= 20) {
            heureRisk = 0.05; // heures normales = quasi aucun risque
        } else {
            heureRisk = 0.35; // 6h, 21h
        }

        // Distance : > 200km = risque eleve
        double distRisk = Math.min(1.0, distanceKm / 250.0);

        // Jours avant : 0-1 jour = risque (derniere minute), > 14 jours = risque (trop tot)
        double joursRisk;
        if (joursAvant <= 1) {
            joursRisk = 0.8;
        } else if (joursAvant <= 3) {
            joursRisk = 0.2;
        } else if (joursAvant <= 7) {
            joursRisk = 0.1;
        } else if (joursAvant <= 14) {
            joursRisk = 0.35;
        } else {
            joursRisk = 0.6;
        }

        // Places : beaucoup de places vides = conducteur peut annuler
        double placesRisk;
        if (nbPlaces >= 4) {
            placesRisk = 0.8;
        } else if (nbPlaces == 3) {
            placesRisk = 0.5;
        } else if (nbPlaces == 2) {
            placesRisk = 0.2;
        } else {
            placesRisk = 0.05;
        }

        // Prix : considerer le ratio prix/km (normal = 0.10-0.15 TND/km)
        double prixParKm = distanceKm > 0 ? prix / distanceKm : 0;
        double prixRisk;
        if (prix < 5) {
            prixRisk = 0.7; // pas serieux
        } else if (prixParKm > 0.30) {
            prixRisk = 0.9; // > 2x le prix normal
        } else if (prixParKm > 0.20) {
            prixRisk = 0.6; // cher
        } else if (prix > 40) {
            prixRisk = 0.7;
        } else if (prix > 30) {
            prixRisk = 0.4;
        } else {
            prixRisk = 0.05; // prix raisonnable
        }

        // Score logique pondere : heure > places > prix > distance > jours
        double logicRisk = heureRisk * 0.30 + placesRisk * 0.25 + prixRisk * 0.20 + distRisk * 0.15 + joursRisk * 0.10;

        // Amplification si plusieurs facteurs a haut risque se combinent
        int highRiskCount = 0;
        if (heureRisk >= 0.7) highRiskCount++;
        if (placesRisk >= 0.7) highRiskCount++;
        if (prixRisk >= 0.5) highRiskCount++;
        if (joursRisk >= 0.6) highRiskCount++;
        if (distRisk >= 0.7) highRiskCount++;
        if (highRiskCount >= 2) {
            logicRisk = Math.min(1.0, logicRisk * 1.3);
        }

        // Score final hybride : LR (15%) + Logique (85%)
        double probability = lrProba * 0.15 + logicRisk * 0.85;
        probability = Math.max(0, Math.min(1, probability));

        String riskLevel;
        if (probability < 0.3) {
            riskLevel = "FAIBLE";
        } else if (probability < 0.55) {
            riskLevel = "MOYEN";
        } else {
            riskLevel = "ELEVE";
        }

        String message;
        switch (riskLevel) {
            case "FAIBLE":
                message = "Risque faible d'annulation. Cette reservation semble fiable.";
                break;
            case "MOYEN":
                message = "Risque moyen d'annulation. Surveillez cette reservation.";
                break;
            default:
                message = "Risque eleve d'annulation. Envisagez des mesures preventives.";
                break;
        }

        return new CancellationResponse(
                Math.round(probability * 10000.0) / 10000.0,
                riskLevel,
                message
        );
    }

    public boolean isReady() {
        return model != null && preprocessor != null;
    }
}

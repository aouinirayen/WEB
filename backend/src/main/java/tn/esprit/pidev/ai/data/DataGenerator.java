package tn.esprit.pidev.ai.data;

import tn.esprit.pidev.ai.math.HaversineCalculator;
import tn.esprit.pidev.entity.AITrainingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final Logger log = LoggerFactory.getLogger(DataGenerator.class);
    private final Random rand = new Random(42);

    public List<AITrainingData> generateMatchingData(int count) {
        log.info("Generating {} matching training examples...", count);
        List<AITrainingData> data = new ArrayList<>();
        TunisianCity[] cities = TunisianCity.CITIES;

        for (int i = 0; i < count; i++) {
            TunisianCity depCity = cities[rand.nextInt(cities.length)];
            TunisianCity destCity;
            do {
                destCity = cities[rand.nextInt(cities.length)];
            } while (destCity.getName().equals(depCity.getName()));

            double routeDistance = HaversineCalculator.calculate(
                    depCity.getLat(), depCity.getLng(), destCity.getLat(), destCity.getLng());

            int heureDepart = 5 + rand.nextInt(17);
            int jourSemaine = 1 + rand.nextInt(7);
            int saison = 1 + rand.nextInt(4);
            int nbPlaces = 1 + rand.nextInt(4);

            double tarifKm = routeDistance < 30 ? 0.15 + rand.nextDouble() * 0.15
                    : 0.06 + rand.nextDouble() * 0.10;
            double prix = routeDistance * tarifKm * (0.8 + rand.nextDouble() * 0.4);

            double passengerLat = depCity.getLat() + (rand.nextDouble() - 0.5) * 0.5;
            double passengerLng = depCity.getLng() + (rand.nextDouble() - 0.5) * 0.5;

            double distPassagerDep = HaversineCalculator.calculate(
                    passengerLat, passengerLng, depCity.getLat(), depCity.getLng());
            double distPassagerDest = HaversineCalculator.calculate(
                    passengerLat, passengerLng, destCity.getLat(), destCity.getLng());

            int heureVoulue = Math.max(5, Math.min(22, heureDepart + (rand.nextInt(5) - 2)));
            double diffHeure = Math.abs(heureDepart - heureVoulue);

            double cosAngle = HaversineCalculator.cosineAngleBetweenVectors(
                    passengerLat, passengerLng, destCity.getLat(), destCity.getLng(),
                    depCity.getLat(), depCity.getLng(), destCity.getLat(), destCity.getLng());

            double budgetPassager = prix * (0.7 + rand.nextDouble() * 0.6);
            double diffPrix = Math.abs(prix - budgetPassager) / Math.max(1, budgetPassager);

            // Score continu pour label plus precis
            double score = 0;
            if (distPassagerDep < 15) score += 0.30;
            else if (distPassagerDep < 30) score += 0.15;
            if (diffHeure < 1) score += 0.25;
            else if (diffHeure < 2) score += 0.15;
            if (diffPrix < 0.3) score += 0.20;
            else if (diffPrix < 0.5) score += 0.10;
            if (cosAngle > 0.7) score += 0.25;
            else if (cosAngle > 0.3) score += 0.10;
            double label = score >= 0.55 ? 1.0 : 0.0;
            if (rand.nextDouble() < 0.05) {
                label = 1.0 - label;
            }

            String features = String.format("%.4f,%.4f,%.4f,%.4f,%.4f,%.4f,%.4f",
                    distPassagerDep, distPassagerDest, diffHeure, diffPrix,
                    (double) nbPlaces, (double) jourSemaine, cosAngle);

            AITrainingData td = new AITrainingData();
            td.setDataType("MATCHING");
            td.setFeatures(features);
            td.setLabel(label);
            td.setDepartureName(depCity.getName());
            td.setDestinationName(destCity.getName());
            td.setDepartureLat(depCity.getLat());
            td.setDepartureLng(depCity.getLng());
            td.setDestinationLat(destCity.getLat());
            td.setDestinationLng(destCity.getLng());
            data.add(td);
        }

        long positives = data.stream().filter(d -> d.getLabel() == 1.0).count();
        log.info("Matching data: {} positives, {} negatives", positives, count - positives);
        return data;
    }

    public List<AITrainingData> generateCancellationData(int count) {
        log.info("Generating {} cancellation training examples...", count);
        List<AITrainingData> data = new ArrayList<>();
        TunisianCity[] cities = TunisianCity.CITIES;

        for (int i = 0; i < count; i++) {
            TunisianCity depCity = cities[rand.nextInt(cities.length)];
            TunisianCity destCity;
            do {
                destCity = cities[rand.nextInt(cities.length)];
            } while (destCity.getName().equals(depCity.getName()));

            double distance = HaversineCalculator.calculate(
                    depCity.getLat(), depCity.getLng(), destCity.getLat(), destCity.getLng());

            double tarifKm = 0.06 + rand.nextDouble() * 0.24;
            double prix = distance * tarifKm * (0.8 + rand.nextDouble() * 0.4);

            int joursAvant = rand.nextInt(31);
            double heure = 5.0 + rand.nextDouble() * 17.0;
            int nbPlaces = 1 + rand.nextInt(4);

            double probAnnulation = 0.1;
            if (prix > 40) probAnnulation += 0.15;
            if (prix > 80) probAnnulation += 0.10;
            if (joursAvant < 2) probAnnulation += 0.20;
            else if (joursAvant < 5) probAnnulation += 0.10;
            if (heure > 20) probAnnulation += 0.10;
            if (nbPlaces == 1) probAnnulation += 0.05;

            probAnnulation = Math.min(0.9, probAnnulation);
            double label = rand.nextDouble() < probAnnulation ? 1.0 : 0.0;

            String features = String.format("%.4f,%.4f,%.4f,%.4f,%.4f",
                    prix, distance, (double) joursAvant, heure, (double) nbPlaces);

            AITrainingData td = new AITrainingData();
            td.setDataType("CANCELLATION");
            td.setFeatures(features);
            td.setLabel(label);
            td.setDepartureName(depCity.getName());
            td.setDestinationName(destCity.getName());
            td.setDepartureLat(depCity.getLat());
            td.setDepartureLng(depCity.getLng());
            td.setDestinationLat(destCity.getLat());
            td.setDestinationLng(destCity.getLng());
            data.add(td);
        }

        long cancelled = data.stream().filter(d -> d.getLabel() == 1.0).count();
        log.info("Cancellation data: {} cancelled, {} confirmed", cancelled, count - cancelled);
        return data;
    }

    public List<AITrainingData> generateSatisfactionData(int count) {
        log.info("Generating {} satisfaction training examples...", count);
        List<AITrainingData> data = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double matchScore = rand.nextDouble();
            double prixRatio = 0.5 + rand.nextDouble() * 1.5;
            double ponctualite = 0.3 + rand.nextDouble() * 0.7;
            double placesRatio = rand.nextDouble();
            double detourKm = rand.nextDouble() * 30.0;

            double note = 1.8 * matchScore + 1.2 * (1.0 - Math.min(1.0, Math.abs(prixRatio - 1.0)))
                    + 1.5 * ponctualite + 0.3 * placesRatio - 0.02 * detourKm;
            note = note * 0.85 + 0.8;
            note += (rand.nextGaussian() * 0.2);
            note = Math.max(1.0, Math.min(5.0, note));

            String features = String.format("%.4f,%.4f,%.4f,%.4f,%.4f",
                    matchScore, prixRatio, ponctualite, placesRatio, detourKm);

            AITrainingData td = new AITrainingData();
            td.setDataType("SATISFACTION");
            td.setFeatures(features);
            td.setLabel(Math.round(note * 100.0) / 100.0);
            td.setDepartureName("");
            td.setDestinationName("");
            data.add(td);
        }

        double avgNote = data.stream().mapToDouble(AITrainingData::getLabel).average().orElse(0);
        log.info("Satisfaction data: average note = {}", String.format("%.2f", avgNote));
        return data;
    }

    public static double[][] parseFeatures(List<AITrainingData> data) {
        double[][] result = new double[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            String[] parts = data.get(i).getFeatures().split(",");
            result[i] = new double[parts.length];
            for (int j = 0; j < parts.length; j++) {
                result[i][j] = Double.parseDouble(parts[j].trim());
            }
        }
        return result;
    }

    public static double[] parseLabels(List<AITrainingData> data) {
        double[] result = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            result[i] = data.get(i).getLabel();
        }
        return result;
    }

    public static double[][] parseLabelsAsMatrix(List<AITrainingData> data) {
        double[][] result = new double[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            result[i][0] = data.get(i).getLabel();
        }
        return result;
    }
}

package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.DriverConfiance;
import tn.esprit.pidev.dto.DriverConfiance.AvisInfo;
import tn.esprit.pidev.dto.DriverConfiance.DetailPoints;
import tn.esprit.pidev.entity.Covoiturage;
import tn.esprit.pidev.entity.DriverRating;
import tn.esprit.pidev.repository.CovoiturageRepository;
import tn.esprit.pidev.repository.DriverRatingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverRatingService {

    private final DriverRatingRepository driverRatingRepo;
    private final CovoiturageRepository covoiturageRepo;

    // Seuils pour l'auto-confirmation
    private static final int SEUIL_COVOITURAGES = 5;  // 5 covoiturages confirmés
    private static final int SEUIL_AVIS = 8;          // 8 avis clients
    private static final double SEUIL_MOYENNE_ETOILES = 3.0;  // Moyenne >= 3.0

    public DriverRatingService(DriverRatingRepository driverRatingRepo, CovoiturageRepository covoiturageRepo) {
        this.driverRatingRepo = driverRatingRepo;
        this.covoiturageRepo = covoiturageRepo;
    }

    /**
     * Calcule les points de confiance pour un conducteur
     * - Covoiturages confirmés × 8 pts (max 40)
     * - Total avis × 3.75 pts (max 30)
     * - (Moyenne étoiles - 3.0) × 15 pts (max 30)
     */
    public DriverConfiance getConfiance(String driverName) {
        DriverConfiance confiance = new DriverConfiance();
        confiance.setDriverName(driverName);

        // 1. Compter les covoiturages confirmés
        int nombreCovoiturages = (int) covoiturageRepo.findAll().stream()
                .filter(c -> c.getDriverName().equalsIgnoreCase(driverName) && "CONFIRMED".equals(c.getStatus()))
                .count();
        
        // Points pour covoiturages (max 40)
        int pointsCovoiturages = Math.min(nombreCovoiturages * 8, 40);
        
        confiance.setNombreCovoituragesConfirmes(nombreCovoiturages);
        confiance.setSeuilCovoiturages(SEUIL_COVOITURAGES);
        confiance.setConditionCovoiturages(nombreCovoiturages >= SEUIL_COVOITURAGES);

        // 2. Compter les avis (manuel et IA)
        List<DriverRating> allRatings = driverRatingRepo.findByDriverName(driverName);
        int avisManuel = (int) allRatings.stream().filter(r -> r.getStars() > 0).count();
        int avisIA = allRatings.size() - avisManuel;
        int totalAvis = allRatings.size();
        
        // Points pour avis (max 30)
        int pointsAvis = Math.min(totalAvis * 3, 30);  // Simplifié: 3 pts par avis
        
        confiance.setNombreAvis(totalAvis);
        confiance.setAvisManuel(avisManuel);
        confiance.setAvisIA(avisIA);
        confiance.setSeuilAvis(SEUIL_AVIS);
        confiance.setConditionAvis(totalAvis >= SEUIL_AVIS);

        // 3. Calculer la moyenne des étoiles
        double moyenneManuel = 0;
        if (avisManuel > 0) {
            moyenneManuel = allRatings.stream()
                    .filter(r -> r.getStars() > 0)
                    .mapToInt(DriverRating::getStars)
                    .average()
                    .orElse(0);
        }
        
        double moyenneIA = 0;
        if (avisIA > 0) {
            moyenneIA = allRatings.stream()
                    .filter(r -> r.getStars() == 0)  // IA: stars = 0
                    .mapToDouble(DriverRating::getPredictedScore)
                    .average()
                    .orElse(0);
        }
        
        double moyenneEtoiles = (avisManuel + avisIA) > 0 
                ? (moyenneManuel * avisManuel + moyenneIA * avisIA) / (avisManuel + avisIA)
                : 0;
        
        // Points pour étoiles (max 30)
        int pointsEtoiles = (int) Math.max(0, Math.min((moyenneEtoiles - 3.0) * 15, 30));
        
        confiance.setMoyenneEtoiles(moyenneEtoiles);
        confiance.setMoyenneManuel(moyenneManuel);
        confiance.setMoyenneIA(moyenneIA);
        confiance.setSeuilMoyenneEtoiles(SEUIL_MOYENNE_ETOILES);
        confiance.setConditionMoyenne(moyenneEtoiles >= SEUIL_MOYENNE_ETOILES);

        // Total des points
        int totalPoints = pointsCovoiturages + pointsAvis + pointsEtoiles;
        confiance.setPointsConfiance(Math.min(totalPoints, 100));

        // Détail des points
        confiance.setDetailPoints(new DetailPoints(pointsCovoiturages, pointsAvis, pointsEtoiles));

        // Auto-confirmation si les 3 conditions sont remplies
        boolean autoConfirm = confiance.isConditionCovoiturages() 
                           && confiance.isConditionAvis() 
                           && confiance.isConditionMoyenne();
        confiance.setAutoConfirmation(autoConfirm);
        confiance.setConducteurDeConfiance(autoConfirm);

        // Liste des avis
        List<AvisInfo> avisList = allRatings.stream()
                .map(r -> new AvisInfo(
                        r.getId(),
                        r.getStars() > 0 ? r.getStars() : (int) Math.round(r.getPredictedScore()),
                        r.getStars() > 0 ? "MANUEL" : "IA",
                        r.getRoute(),
                        r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
                ))
                .collect(Collectors.toList());
        confiance.setAvisList(avisList);

        return confiance;
    }

    /**
     * Ajoute un avis manuel pour un covoiturage
     */
    public DriverConfiance addAvis(Long covoiturageId, int stars) {
        Covoiturage covoiturage = covoiturageRepo.findById(covoiturageId)
                .orElseThrow(() -> new RuntimeException("Covoiturage non trouvé"));

        DriverRating rating = new DriverRating();
        rating.setDriverName(covoiturage.getDriverName());
        rating.setCovoiturageId(covoiturageId);
        rating.setRoute(covoiturage.getDeparture() + " -> " + covoiturage.getDestination());
        rating.setStars(stars);
        rating.setPredictedScore(stars);  // Pour les avis manuels, predictedScore = stars
        rating.setCreatedAt(LocalDateTime.now());
        
        driverRatingRepo.save(rating);

        // Recalculer la confiance après ajout
        return getConfiance(covoiturage.getDriverName());
    }

    /**
     * Confirme un covoiturage (manuel) et vérifie l'auto-confirmation
     */
    public DriverConfiance confirmerCovoiturage(Long covoiturageId) {
        Covoiturage covoiturage = covoiturageRepo.findById(covoiturageId)
                .orElseThrow(() -> new RuntimeException("Covoiturage non trouvé"));

        covoiturage.setStatus("CONFIRMED");
        covoiturageRepo.save(covoiturage);

        // Recalculer la confiance après confirmation
        return getConfiance(covoiturage.getDriverName());
    }

    /**
     * Génère des données de test pour le système de confiance
     */
    public String seedTestConfiance() {
        // NE PAS supprimer les données existantes pour éviter la perte de données
        // driverRatingRepo.deleteAll();  // Commenté pour préserver les données

        // Créer un conducteur de test avec des avis
        String driverName = "Fatma Trabelsi";
        
        // Vérifier si des avis existent déjà pour ce conducteur
        List<DriverRating> existingRatings = driverRatingRepo.findByDriverName(driverName);
        if (!existingRatings.isEmpty()) {
            return "Données de test existent déjà pour " + driverName + " - " + existingRatings.size() + " avis trouvés. Utilisez les données existantes.";
        }
        
        // Ajouter des avis IA (stars = 0) - 4 avis avec score 4.0-4.5
        for (int i = 0; i < 4; i++) {
            DriverRating rating = new DriverRating();
            rating.setDriverName(driverName);
            rating.setCovoiturageId((long) i);
            rating.setRoute("Tunis -> Sousse");
            rating.setStars(0);  // IA
            rating.setPredictedScore(4.0 + Math.random() * 0.5);
            rating.setCreatedAt(LocalDateTime.now());
            driverRatingRepo.save(rating);
        }

        // Ajouter des avis utilisateurs (stars > 0) - 5 avis avec 4-5 étoiles
        for (int i = 4; i < 9; i++) {
            DriverRating rating = new DriverRating();
            rating.setDriverName(driverName);
            rating.setCovoiturageId((long) i);
            rating.setRoute("Tunis -> Sousse");
            rating.setStars(4 + (int)(Math.random() * 2));  // 4 ou 5 étoiles
            rating.setPredictedScore(rating.getStars());
            rating.setCreatedAt(LocalDateTime.now());
            driverRatingRepo.save(rating);
        }

        // Créer 5 covoiturages confirmés (condition 1)
        for (int i = 0; i < 5; i++) {
            Covoiturage covoiturage = new Covoiturage();
            covoiturage.setDriverName(driverName);
            covoiturage.setDeparture("Tunis");
            covoiturage.setDestination("Sousse");
            covoiturage.setDate(java.time.LocalDate.now().plusDays(i));
            covoiturage.setHeureDepart("08:00");
            covoiturage.setHeureArrivee("10:00");
            covoiturage.setPrice(20.0);
            covoiturage.setAvailableSeats(4);
            covoiturage.setVehicle("Peugeot 308");
            covoiturage.setStatus("CONFIRMED");
            covoiturageRepo.save(covoiturage);
        }

        // Conditions vérifiées :
        // - 5 covoiturages confirmés ✓
        // - 9 avis (4 IA + 5 utilisateurs) >= 8 ✓
        // - Moyenne ~4.3 >= 3.0 ✓

        return "Données de test créées avec succès pour Fatma Trablesi - 5 covoiturages confirmés, 9 avis, moyenne ~4.3 - Auto-confirmation ACTIVE !";
    }

    /**
     * Récupère les avis d'un conducteur
     */
    public List<AvisInfo> getAvisByDriver(String driverName) {
        return driverRatingRepo.findByDriverName(driverName).stream()
                .map(r -> new AvisInfo(
                        r.getId(),
                        r.getStars() > 0 ? r.getStars() : (int) Math.round(r.getPredictedScore()),
                        r.getStars() > 0 ? "MANUEL" : "IA",
                        r.getRoute(),
                        r.getCreatedAt() != null ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
                ))
                .collect(Collectors.toList());
    }
}

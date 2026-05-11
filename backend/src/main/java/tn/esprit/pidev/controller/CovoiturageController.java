package tn.esprit.pidev.controller;



import tn.esprit.pidev.dto.CovoiturageDTO;
import tn.esprit.pidev.dto.DriverConfiance;
import tn.esprit.pidev.dto.DriverConfiance.AvisInfo;

import tn.esprit.pidev.entity.Covoiturage;

import tn.esprit.pidev.service.CovoiturageService;
import tn.esprit.pidev.service.DriverRatingService;

import org.springframework.web.bind.annotation.*;



import java.util.List;



@RestController

@RequestMapping("/api/covoiturages")

@CrossOrigin("*")

public class CovoiturageController {



    private final CovoiturageService service;
    private final DriverRatingService driverRatingService;



    public CovoiturageController(CovoiturageService service, DriverRatingService driverRatingService) {

        this.service = service;
        this.driverRatingService = driverRatingService;

    }



    // CREATE

    @PostMapping

    public Covoiturage add(@RequestBody CovoiturageDTO dto) {

        Covoiturage c = new Covoiturage();

        c.setDriverName(dto.getDriverName());

        c.setDeparture(dto.getDeparture());

        c.setDestination(dto.getDestination());

        c.setDate(dto.getDate());

        c.setHeureDepart(dto.getHeureDepart());

        c.setHeureArrivee(dto.getHeureArrivee());

        c.setPrice(dto.getPrice());

        c.setAvailableSeats(dto.getAvailableSeats());

        c.setVehicle(dto.getVehicle());

        c.setStatus(dto.getStatus());

        c.setDepartureLat(dto.getDepartureLat());

        c.setDepartureLng(dto.getDepartureLng());

        c.setDestinationLat(dto.getDestinationLat());

        c.setDestinationLng(dto.getDestinationLng());

        Covoiturage savedCovoiturage = service.add(c);

        // Auto-confirm if driver has auto-confirmation enabled
        DriverConfiance confiance = driverRatingService.getConfiance(dto.getDriverName());
        if (confiance.isAutoConfirmation()) {
            savedCovoiturage.setStatus("CONFIRMED");
            savedCovoiturage = service.update(savedCovoiturage.getId(), savedCovoiturage);
        }

        return savedCovoiturage;

    }



    // READ

    @GetMapping

    public List<Covoiturage> getAll() {

        return service.getAll();

    }



    // UPDATE

    @PutMapping("/{id}")

    public Covoiturage update(@PathVariable Long id, @RequestBody CovoiturageDTO dto) {

        Covoiturage c = new Covoiturage();

        c.setDriverName(dto.getDriverName());

        c.setDeparture(dto.getDeparture());

        c.setDestination(dto.getDestination());

        c.setDate(dto.getDate());

        c.setHeureDepart(dto.getHeureDepart());

        c.setHeureArrivee(dto.getHeureArrivee());

        c.setPrice(dto.getPrice());

        c.setAvailableSeats(dto.getAvailableSeats());

        c.setVehicle(dto.getVehicle());

        c.setStatus(dto.getStatus());

        c.setDepartureLat(dto.getDepartureLat());

        c.setDepartureLng(dto.getDepartureLng());

        c.setDestinationLat(dto.getDestinationLat());

        c.setDestinationLng(dto.getDestinationLng());

        return service.update(id, c);

    }



    // DELETE

    @DeleteMapping("/{id}")

    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Covoiturage deleted";

    }


    // === CONFIANCE & AVIS ENDPOINTS ===

    /**
     * Récupère les informations de confiance d'un conducteur
     * Auto-confirmation SI:
     * - 5 covoiturages confirmés
     * - Plus de 8 avis clients
     * - Moyenne >= 4.0
     */
    @GetMapping("/confiance/{driverName}")
    public DriverConfiance getConfiance(@PathVariable String driverName) {
        return driverRatingService.getConfiance(driverName);
    }

    /**
     * Ajoute un avis manuel pour un covoiturage
     */
    @PostMapping("/{id}/avis")
    public DriverConfiance addAvis(@PathVariable Long id, @RequestParam int stars) {
        if (stars < 1 || stars > 5) {
            throw new RuntimeException("Les étoiles doivent être entre 1 et 5");
        }
        return driverRatingService.addAvis(id, stars);
    }

    /**
     * Confirme un covoiturage (manuel) et vérifie l'auto-confirmation
     */
    @PutMapping("/{id}/confirmer")
    public DriverConfiance confirmerCovoiturage(@PathVariable Long id) {
        return driverRatingService.confirmerCovoiturage(id);
    }

    /**
     * Génère des données de test pour le système de confiance
     */
    @PostMapping("/seed-test-confiance")
    public String seedTestConfiance() {
        return driverRatingService.seedTestConfiance();
    }

    /**
     * Récupère la liste des avis d'un conducteur
     */
    @GetMapping("/avis/{driverName}")
    public List<AvisInfo> getAvisByDriver(@PathVariable String driverName) {
        return driverRatingService.getAvisByDriver(driverName);
    }

}


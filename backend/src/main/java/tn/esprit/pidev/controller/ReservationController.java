package tn.esprit.pidev.controller;



import tn.esprit.pidev.dto.ReservationDTO;

import tn.esprit.pidev.entity.Reservation;

import tn.esprit.pidev.repository.ReservationRepository;

import tn.esprit.pidev.entity.Covoiturage;

import tn.esprit.pidev.repository.CovoiturageRepository;

import tn.esprit.pidev.service.ReservationService;

import tn.esprit.pidev.service.ReservationEmailService;

import org.springframework.web.bind.annotation.*;



import java.util.ArrayList;

import java.util.List;

import java.util.Map;



@RestController

@RequestMapping("/api/reservations")

@CrossOrigin("*")

public class ReservationController {



    private final ReservationService service;

    private final ReservationEmailService emailService;

    private final ReservationRepository reservationRepo;

    private final CovoiturageRepository covoiturageRepo;



    public ReservationController(ReservationService service, ReservationEmailService emailService,

                                 ReservationRepository reservationRepo, CovoiturageRepository covoiturageRepo) {

        this.service = service;

        this.emailService = emailService;

        this.reservationRepo = reservationRepo;

        this.covoiturageRepo = covoiturageRepo;

    }



    // CREATE

    @PostMapping

    public Reservation add(@RequestBody ReservationDTO dto) {

        Reservation r = new Reservation();

        r.setClientName(dto.getClientName());

        r.setPhone(dto.getPhone());

        r.setEmail(dto.getEmail());

        r.setSeatsReserved(dto.getSeatsReserved());

        r.setBookingDate(dto.getBookingDate());

        r.setStatus(dto.getStatus());

        r.setCovoiturageId(dto.getCovoiturageId());

        r.setClientLat(dto.getClientLat());

        r.setClientLng(dto.getClientLng());

        r.setClientAddress(dto.getClientAddress());

        r.setDisplacementRequested(dto.getDisplacementRequested());

        r.setDisplacementPrice(dto.getDisplacementPrice());

        

        Reservation saved = service.add(r);

        

        // Send confirmation email asynchronously

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {

            emailService.sendConfirmationEmail(saved, dto.getEmail());

        }

        

        return saved;

    }



    // READ

    @GetMapping

    public List<Reservation> getAll() {

        return service.getAll();

    }



    // UPDATE

    @PutMapping("/{id}")

    public Reservation update(@PathVariable Long id, @RequestBody ReservationDTO dto) {

        // Get old reservation to detect status change

        Reservation old = service.getAll().stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);

        String oldStatus = old != null ? old.getStatus() : null;



        Reservation r = new Reservation();

        r.setClientName(dto.getClientName());

        r.setPhone(dto.getPhone());

        r.setEmail(dto.getEmail());

        r.setSeatsReserved(dto.getSeatsReserved());

        r.setBookingDate(dto.getBookingDate());

        r.setStatus(dto.getStatus());

        r.setCovoiturageId(dto.getCovoiturageId());

        r.setClientLat(dto.getClientLat());

        r.setClientLng(dto.getClientLng());

        r.setClientAddress(dto.getClientAddress());

        r.setDisplacementRequested(dto.getDisplacementRequested());

        r.setDisplacementPrice(dto.getDisplacementPrice());

        Reservation updated = service.update(id, r);



        // Send email on status change (CONFIRMED or REJECTED)

        String newStatus = dto.getStatus();

        if (newStatus != null && !newStatus.equals(oldStatus)

                && ("CONFIRMED".equals(newStatus) || "REJECTED".equals(newStatus))) {

            String email = updated.getEmail();

            if (email != null && !email.isBlank()) {

                emailService.sendStatusUpdateEmail(updated, email, newStatus);

            }

        }



        return updated;

    }



    // CHECK ELIGIBILITY TO RATE

    @GetMapping("/can-rate")

    public Map<String, Boolean> canRate(@RequestParam Long covoiturageId, @RequestParam String clientName) {

        boolean eligible = !reservationRepo.findByCovoiturageIdAndStatus(covoiturageId, "CONFIRMED").isEmpty()

            && reservationRepo.findByClientNameIgnoreCaseAndStatus(clientName, "CONFIRMED").stream()

                .anyMatch(r -> r.getCovoiturageId() != null && r.getCovoiturageId().equals(covoiturageId));

        return Map.of("canRate", eligible);

    }



    // GET RATABLE COVOITURAGES (confirmed reservations for a client with completed trips)

    @GetMapping("/ratable-covoiturages")

    public List<Map<String, Object>> getRatableCovoiturages(@RequestParam String clientName) {

        try {

            System.out.println("DEBUG: Searching for completed covoiturages for client: " + clientName);

            List<Map<String, Object>> result = new ArrayList<>();

            List<Reservation> confirmed = reservationRepo.findByClientNameIgnoreCaseAndStatus(clientName, "CONFIRMED");

            System.out.println("DEBUG: Found " + confirmed.size() + " confirmed reservations");

            

            // Get current date

            java.time.LocalDate today = java.time.LocalDate.now();

            

            for (Reservation r : confirmed) {

                System.out.println("DEBUG: Processing reservation ID: " + r.getId() + ", covoiturageId: " + r.getCovoiturageId());

                if (r.getCovoiturageId() != null) {

                    Covoiturage c = covoiturageRepo.findById(r.getCovoiturageId()).orElse(null);

                    if (c != null) {

                        // Check if covoiturage date is in the past (trip completed)

                        java.time.LocalDate tripDate = c.getDate();

                        if (tripDate.isBefore(today) || tripDate.isEqual(today)) {

                            String label = c.getDeparture() + " → " + c.getDestination()

                                    + " (" + c.getDate() + ") - " + c.getDriverName();

                            result.add(Map.of("id", r.getCovoiturageId(), "label", label));

                            System.out.println("DEBUG: Added completed covoiturage: " + label);

                        } else {

                            System.out.println("DEBUG: Skipping future covoiturage: " + c.getDate() + " (not yet completed)");

                        }

                    } else {

                        System.out.println("DEBUG: Covoiturage not found for ID: " + r.getCovoiturageId());

                    }

                }

            }

            System.out.println("DEBUG: Returning " + result.size() + " completed covoiturages for rating");

            return result;

        } catch (Exception e) {

            System.err.println("ERROR in getRatableCovoiturages: " + e.getMessage());

            e.printStackTrace();

            return new ArrayList<>();

        }

    }



    // DELETE

    @DeleteMapping("/{id}")

    public String delete(@PathVariable Long id) {

        service.delete(id);

        return "Reservation deleted";

    }

}


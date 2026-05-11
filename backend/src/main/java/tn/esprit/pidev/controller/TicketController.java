package tn.esprit.pidev.controller;

import tn.esprit.pidev.dto.TicketDTO;
import tn.esprit.pidev.entity.Ticket;
import tn.esprit.pidev.entity.TransportType;
import tn.esprit.pidev.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin("*")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    // CREATE — Admin ajoute un ticket
    @PostMapping
    public ResponseEntity<?> add(@RequestBody TicketDTO dto) {
        List<String> errors = validateTicket(dto);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        Ticket t = mapDtoToTicket(dto);
        return ResponseEntity.ok(service.add(t));
    }

    // READ — Tous les tickets
    @GetMapping
    public List<Ticket> getAll() {
        return service.getAll();
    }

    // READ — Tickets disponibles uniquement
    @GetMapping("/disponibles")
    public List<Ticket> getDisponibles() {
        return service.getDisponibles();
    }

    // READ — Tickets par type de transport
    @GetMapping("/transport/{type}")
    public List<Ticket> getByTransport(@PathVariable String type) {
        TransportType transportType = TransportType.valueOf(type.toUpperCase());
        return service.getByTransportType(transportType);
    }

    // READ — Tickets disponibles par type de transport
    @GetMapping("/disponibles/{type}")
    public List<Ticket> getDisponiblesByTransport(@PathVariable String type) {
        TransportType transportType = TransportType.valueOf(type.toUpperCase());
        return service.getDisponiblesByTransportType(transportType);
    }

    // READ — Liste des types de transport disponibles
    @GetMapping("/transport-types")
    public List<Map<String, String>> getTransportTypes() {
        return Arrays.stream(TransportType.values())
                .map(t -> Map.of("value", t.name(), "label", t.getDisplayName()))
                .toList();
    }

    // UPDATE — Admin modifie un ticket
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TicketDTO dto) {
        List<String> errors = validateTicket(dto);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        Ticket t = mapDtoToTicket(dto);
        return ResponseEntity.ok(service.update(id, t));
    }

    // ACHETER — Un utilisateur achète un ticket (décrément la quantité)
    @PostMapping("/{id}/acheter")
    public ResponseEntity<?> acheter(@PathVariable Long id,
                                    @RequestParam(required = false) Long passengerId) {
        try {
            Ticket ticket = service.acheterTicket(id, passengerId);
            return ResponseEntity.ok(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE — Admin supprime un ticket
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Ticket deleted";
    }

    // SEED — Populate database with test tickets
    @PostMapping("/seed-data/init")
    public ResponseEntity<?> seedTestData() {
        return ResponseEntity.ok(service.seedTestData());
    }

    // ============================================
    // VALIDATION — Contrôle de saisie
    // ============================================
    private List<String> validateTicket(TicketDTO dto) {
        List<String> errors = new java.util.ArrayList<>();

        // Type obligatoire (Standard, VIP, etc.)
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            errors.add("Le type du ticket est obligatoire (ex: Standard, VIP)");
        }

        // Prix > 0
        if (dto.getPrice() <= 0) {
            errors.add("Le prix doit être supérieur à 0 TND");
        }
        if (dto.getPrice() > 500) {
            errors.add("Le prix ne peut pas dépasser 500 TND");
        }

        // Validité obligatoire
        if (dto.getValidity() == null || dto.getValidity().trim().isEmpty()) {
            errors.add("La validité est obligatoire (ex: 1 jour, 1 trajet)");
        }

        // Transport type obligatoire et valide
        if (dto.getTransportType() == null || dto.getTransportType().trim().isEmpty()) {
            errors.add("Le type de transport est obligatoire (BUS, METRO, TRAIN, LOUAGE, BATEAU)");
        } else {
            try {
                TransportType.valueOf(dto.getTransportType().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Type de transport invalide. Valeurs acceptées : BUS, METRO, TRAIN, LOUAGE, BATEAU");
            }
        }

        // Quantité disponible >= 1
        if (dto.getQuantiteDisponible() < 1) {
            errors.add("La quantité disponible doit être au minimum 1");
        }
        if (dto.getQuantiteDisponible() > 10000) {
            errors.add("La quantité disponible ne peut pas dépasser 10000");
        }

        // Lieu de départ obligatoire
        if (dto.getLieuDepart() == null || dto.getLieuDepart().trim().isEmpty()) {
            errors.add("Le lieu de départ est obligatoire");
        }

        // Destination obligatoire
        if (dto.getDestination() == null || dto.getDestination().trim().isEmpty()) {
            errors.add("La destination est obligatoire");
        }

        // Départ et destination différents
        if (dto.getLieuDepart() != null && dto.getDestination() != null
                && dto.getLieuDepart().trim().equalsIgnoreCase(dto.getDestination().trim())) {
            errors.add("Le lieu de départ et la destination doivent être différents");
        }

        // Heure de départ obligatoire
        if (dto.getHeureDepart() == null || dto.getHeureDepart().trim().isEmpty()) {
            errors.add("L'heure de départ est obligatoire (ex: 08:00)");
        } else if (!dto.getHeureDepart().matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) {
            errors.add("L'heure de départ doit être au format HH:MM (ex: 08:00, 14:30)");
        }

        // Description max 200 caractères
        if (dto.getDescription() != null && dto.getDescription().length() > 200) {
            errors.add("La description ne peut pas dépasser 200 caractères");
        }

        return errors;
    }

    private Ticket mapDtoToTicket(TicketDTO dto) {
        Ticket t = new Ticket();
        t.setType(dto.getType().trim());
        t.setPrice(dto.getPrice());
        t.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        t.setValidity(dto.getValidity().trim());
        t.setQuantiteDisponible(dto.getQuantiteDisponible());
        t.setLieuDepart(dto.getLieuDepart().trim());
        t.setDestination(dto.getDestination().trim());
        t.setHeureDepart(dto.getHeureDepart().trim());
        t.setTransportType(TransportType.valueOf(dto.getTransportType().toUpperCase().trim()));
        return t;
    }
}

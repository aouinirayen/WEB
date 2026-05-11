package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.*;
import tn.esprit.pidev.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import tn.esprit.pidev.service.GlobalNotificationService;
import tn.esprit.pidev.service.GlobalNotificationService;

@Service
public class TicketService {

    private final TicketRepository repo;
    private final UserRepository userRepo;
    private final LoyaltyAccountRepository loyaltyRepo;
    private final PointTransactionRepository txRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final GlobalNotificationService globalNotif;

    public TicketService(TicketRepository repo,
                         UserRepository userRepo,
                         LoyaltyAccountRepository loyaltyRepo,
                         PointTransactionRepository txRepo,
                         SubscriptionRepository subscriptionRepo,
                         GlobalNotificationService globalNotif) {
        this.globalNotif = globalNotif;
        this.repo = repo;
        this.userRepo = userRepo;
        this.loyaltyRepo = loyaltyRepo;
        this.txRepo = txRepo;
        this.subscriptionRepo = subscriptionRepo;
    }

    public Ticket add(Ticket t) {
        t.setDisponible(t.getQuantiteDisponible() > 0);
        return repo.save(t);
    }

    public List<Ticket> getAll() { return repo.findAll(); }
    public List<Ticket> getDisponibles() { return repo.findByDisponibleTrue(); }
    public List<Ticket> getByTransportType(TransportType transportType) { return repo.findByTransportType(transportType); }
    public List<Ticket> getDisponiblesByTransportType(TransportType transportType) { return repo.findByTransportTypeAndDisponibleTrue(transportType); }

    public Ticket update(Long id, Ticket t) {
        Ticket old = repo.findById(id).orElseThrow(() -> new RuntimeException("Ticket non trouve: " + id));
        old.setType(t.getType());
        old.setPrice(t.getPrice());
        old.setDescription(t.getDescription());
        old.setValidity(t.getValidity());
        old.setTransportType(t.getTransportType());
        old.setQuantiteDisponible(t.getQuantiteDisponible());
        old.setLieuDepart(t.getLieuDepart());
        old.setDestination(t.getDestination());
        old.setHeureDepart(t.getHeureDepart());
        return repo.save(old);
    }

    /**
     * Achat d un ticket avec logique loyalty complete:
     * 1. Verifie dispo
     * 2. Verifie si subscription active -> reduction 20%
     * 3. Applique reduction selon tier loyalty (SILVER=10%, GOLD=20%)
     * 4. Decremente stock
     * 5. Credite points loyalty (1 point par DT paye)
     */
    public Ticket acheterTicket(Long ticketId, Long passengerId) {
        Ticket ticket = repo.findById(ticketId).orElseThrow(() ->
                new RuntimeException("Ticket non trouve: " + ticketId));

        if (!ticket.isDisponible() || ticket.getQuantiteDisponible() <= 0) {
            throw new RuntimeException("Ticket epuise - plus de tickets disponibles");
        }

        User passenger = userRepo.findById(passengerId).orElse(null);
        double prixFinal = ticket.getPrice();
        String reductionDescription = "";

        if (passenger != null) {
            // 1. Verifier subscription active pour ce type de transport
            boolean hasActiveSubscription = subscriptionRepo
                    .findByPassengerId(passengerId)
                    .stream()
                    .anyMatch(s -> s.getStatut() == SubscriptionStatus.ACTIVE
                            && s.getDateFin().isAfter(LocalDate.now())
                            && s.getPricingPlan() != null
                            && (s.getPricingPlan().getTransportType() == null
                                || s.getPricingPlan().getTransportType() == ticket.getTransportType()));

            if (hasActiveSubscription) {
                prixFinal = prixFinal * 0.80; // 20% reduction abonne
                reductionDescription = " (abonne -20%)";
            } else {
                // 2. Reduction selon tier loyalty
                LoyaltyAccount la = loyaltyRepo.findByPassengerId(passengerId).orElse(null);
                if (la != null) {
                    if (la.getNiveau() == LoyaltyTier.GOLD) {
                        prixFinal = prixFinal * 0.80;
                        reductionDescription = " (GOLD -20%)";
                    } else if (la.getNiveau() == LoyaltyTier.SILVER) {
                        prixFinal = prixFinal * 0.90;
                        reductionDescription = " (SILVER -10%)";
                    }
                }
            }

            // 3. Crediter points loyalty (1 point par DT paye, min 1)
            int pointsGagnes = Math.max(1, (int) Math.floor(prixFinal));
            LoyaltyAccount account = loyaltyRepo.findByPassengerId(passengerId)
                    .orElseGet(() -> loyaltyRepo.save(new LoyaltyAccount(passenger)));
            account.setPointsCumules(account.getPointsCumules() + pointsGagnes);
            account.setNiveau(calculerTier(account.getPointsCumules()));
            loyaltyRepo.save(account);

            txRepo.save(new PointTransaction(
                    pointsGagnes, TransactionType.EARNED, LocalDateTime.now(),
                    "Points gagnes - ticket: " + ticket.getDescription() + reductionDescription,
                    account));
        }

        // 4. Notifier le passager
        if (passenger != null) {
            globalNotif.notify(passenger, "Ticket achete",
                "Vous avez achete: " + ticket.getDescription() + " (" + ticket.getLieuDepart() + " -> " + ticket.getDestination() + ")" + reductionDescription);
            globalNotif.notifyAllAdmins("Vente ticket",
                "Le passager " + passenger.getName() + " a achete: " + ticket.getDescription());
        }

        // 5. Decremente stock
        ticket.setQuantiteDisponible(ticket.getQuantiteDisponible() - 1);
        return repo.save(ticket);
    }

    // Ancien achat sans passenger (compatibilite)
    public Ticket acheterTicket(Long id) {
        return acheterTicket(id, null);
    }

    public void delete(Long id) { repo.deleteById(id); }

    private LoyaltyTier calculerTier(int pts) {
        if (pts >= 500) return LoyaltyTier.GOLD;
        if (pts >= 200) return LoyaltyTier.SILVER;
        return LoyaltyTier.BRONZE;
    }

    public String seedTestData() {
        repo.deleteAll();
        Ticket[] tickets = {
            createTicket("Standard", 0.5, "Bus urbain Tunis", "1 trajet", TransportType.BUS, 100, "Tunis Centre", "Ezzahra", "08:00"),
            createTicket("Standard", 1.5, "Bus interurbain Tunis-Sfax", "1 trajet", TransportType.BUS, 50, "Tunis", "Sfax", "06:30"),
            createTicket("Standard", 0.8, "Metro leger TGM", "1 trajet", TransportType.METRO, 200, "Tunis", "La Marsa", "07:00"),
            createTicket("Standard", 1.2, "Train banlieue", "1 trajet", TransportType.TRAIN, 150, "Tunis", "Rades", "09:00"),
            createTicket("Premium", 18.0, "Train Tunis-Sfax", "1 trajet", TransportType.TRAIN, 75, "Tunis", "Sfax", "10:15"),
            createTicket("Abonnement", 15.0, "Bus etudiant mensuel", "30 jours", TransportType.BUS, 300, "Tunis", "Zone Urbaine", "00:00"),
            createTicket("Reduit", 2.5, "Train etudiant", "1 trajet", TransportType.TRAIN, 120, "Tunis", "Bizerte", "11:30"),
            createTicket("Abonnement", 20.0, "Metro abonnement", "30 jours", TransportType.METRO, 250, "Tunis", "Suburbs", "00:00"),
            createTicket("VIP", 5.0, "Bus VIP climatise", "1 trajet", TransportType.BUS, 40, "Tunis", "Nabeul", "08:30"),
            createTicket("Standard", 12.0, "Louage collectif", "1 trajet", TransportType.LOUAGE, 8, "Tunis", "Kairouan", "07:00"),
            createTicket("Standard", 7.5, "Bus Tunis-Nabeul", "1 trajet", TransportType.BUS, 60, "Tunis", "Nabeul", "09:30"),
            createTicket("Standard", 4.0, "Train regional", "1 trajet", TransportType.TRAIN, 90, "Tunis", "Mahdia", "13:00"),
        };
        repo.saveAll(java.util.Arrays.asList(tickets));
        return "Successfully seeded " + tickets.length + " test tickets";
    }

    private Ticket createTicket(String type, double price, String desc, String validity,
                                TransportType tt, int qty, String dep, String dest, String heure) {
        Ticket t = new Ticket();
        t.setType(type); t.setPrice(price); t.setDescription(desc);
        t.setValidity(validity); t.setTransportType(tt);
        t.setQuantiteDisponible(qty); t.setLieuDepart(dep);
        t.setDestination(dest); t.setHeureDepart(heure);
        return t;
    }
}

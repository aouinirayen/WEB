package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.Reservation;
import tn.esprit.pidev.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.service.GlobalNotificationService;
import tn.esprit.pidev.repository.UserRepository;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository repo;
    private final GlobalNotificationService globalNotif;
    private final UserRepository userRepo;

    public ReservationService(ReservationRepository repo,
                              GlobalNotificationService globalNotif,
                              UserRepository userRepo) {
        this.repo = repo;
        this.globalNotif = globalNotif;
        this.userRepo = userRepo;
    }

    public Reservation add(Reservation r) {
        Reservation saved = repo.save(r);
        // Notifier le passager via email/nom
        if (r.getEmail() != null) {
            userRepo.findByEmail(r.getEmail()).ifPresent(user ->
                globalNotif.notify(user, "Reservation confirmee",
                    "Votre reservation pour " + r.getClientName()
                    + " - " + r.getSeatsReserved() + " place(s) a ete confirmee."));
        }
        globalNotif.notifyAllAdmins("Nouvelle reservation",
            "Reservation de " + r.getClientName() + " - " + r.getSeatsReserved() + " place(s)");
        return saved;
    }

    public List<Reservation> getAll() {
        return repo.findAll();
    }

    public Reservation update(Long id, Reservation r) {
        Reservation old = repo.findById(id).orElseThrow();
        old.setClientName(r.getClientName());
        old.setPhone(r.getPhone());
        old.setEmail(r.getEmail());
        old.setSeatsReserved(r.getSeatsReserved());
        old.setBookingDate(r.getBookingDate());
        old.setStatus(r.getStatus());
        old.setCovoiturageId(r.getCovoiturageId());
        old.setClientLat(r.getClientLat());
        old.setClientLng(r.getClientLng());
        old.setClientAddress(r.getClientAddress());
        old.setDisplacementRequested(r.getDisplacementRequested());
        old.setDisplacementPrice(r.getDisplacementPrice());
        return repo.save(old);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

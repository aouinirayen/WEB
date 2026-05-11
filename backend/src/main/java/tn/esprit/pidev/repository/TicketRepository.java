package tn.esprit.pidev.repository;

import tn.esprit.pidev.entity.Ticket;
import tn.esprit.pidev.entity.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByTransportType(TransportType transportType);

    List<Ticket> findByDisponibleTrue();

    List<Ticket> findByTransportTypeAndDisponibleTrue(TransportType transportType);
}

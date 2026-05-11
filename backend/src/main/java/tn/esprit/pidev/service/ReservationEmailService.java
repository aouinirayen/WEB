package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.Covoiturage;
import tn.esprit.pidev.entity.Reservation;
import tn.esprit.pidev.repository.CovoiturageRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class ReservationEmailService {

    private final JavaMailSender mailSender;
    private final CovoiturageRepository covoiturageRepo;

    public ReservationEmailService(JavaMailSender mailSender, CovoiturageRepository covoiturageRepo) {
        this.mailSender = mailSender;
        this.covoiturageRepo = covoiturageRepo;
    }

    @Async
    public void sendConfirmationEmail(Reservation reservation, String recipientEmail) {
        if (recipientEmail == null || recipientEmail.isBlank()) return;

        Covoiturage cov = null;
        if (reservation.getCovoiturageId() != null) {
            cov = covoiturageRepo.findById(reservation.getCovoiturageId()).orElse(null);
        }

        String subject = "Confirmation de reservation #" + reservation.getId();
        String html = buildEmailHtml(reservation, cov);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("kacem.benbrahim07@gmail.com");
            mailSender.send(message);
            System.out.println("Email de confirmation envoye a " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
        }
    }

    @Async
    public void sendStatusUpdateEmail(Reservation reservation, String recipientEmail, String newStatus) {
        if (recipientEmail == null || recipientEmail.isBlank()) return;

        Covoiturage cov = null;
        if (reservation.getCovoiturageId() != null) {
            cov = covoiturageRepo.findById(reservation.getCovoiturageId()).orElse(null);
        }

        String subject = "Mise a jour de votre reservation #" + reservation.getId() + " - " + newStatus;
        String html = buildStatusUpdateHtml(reservation, cov, newStatus);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("kacem.benbrahim07@gmail.com");
            mailSender.send(message);
            System.out.println("Email de mise a jour envoye a " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
        }
    }

    private String buildStatusUpdateHtml(Reservation reservation, Covoiturage cov, String status) {
        boolean confirmed = "CONFIRMED".equalsIgnoreCase(status);
        String color = confirmed ? "#2e7d32" : "#c62828";
        String bg = confirmed ? "#e8f5e9" : "#ffebee";
        String label = confirmed ? "CONFIRMEE" : "REJETEE";
        String icon = confirmed ? "&#10004;" : "&#10008;";
        String msg = confirmed
                ? "Votre reservation a ete confirmee par le conducteur. Bon voyage !"
                : "Votre reservation a ete rejetee par le conducteur.";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;'>");
        sb.append("<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.1);'>");

        sb.append("<div style='background:linear-gradient(135deg,").append(confirmed ? "#004d40,#00897b" : "#b71c1c,#e53935").append(");color:#fff;padding:24px;text-align:center;'>");
        sb.append("<h1 style='margin:0;font-size:22px;'>Reservation ").append(label).append("</h1>");
        sb.append("<p style='margin:8px 0 0;opacity:0.9;'>Reservation #").append(reservation.getId()).append("</p>");
        sb.append("</div>");

        sb.append("<div style='padding:24px;'>");

        // Status
        sb.append("<div style='margin-bottom:20px;padding:14px;background:").append(bg).append(";border-radius:8px;text-align:center;'>");
        sb.append("<span style='color:").append(color).append(";font-weight:700;font-size:18px;'>").append(icon).append(" ").append(label).append("</span>");
        sb.append("<p style='color:#666;margin:6px 0 0;font-size:13px;'>").append(msg).append("</p>");
        sb.append("</div>");

        // Client info
        sb.append("<h3 style='color:#004d40;border-bottom:2px solid #e0e0e0;padding-bottom:8px;'>Informations</h3>");
        sb.append("<table style='width:100%;border-collapse:collapse;'>");
        sb.append("<tr><td style='padding:6px 0;color:#666;width:40%;'>Nom :</td><td style='padding:6px 0;font-weight:600;'>").append(reservation.getClientName()).append("</td></tr>");
        sb.append("<tr><td style='padding:6px 0;color:#666;'>Places :</td><td style='padding:6px 0;font-weight:600;'>").append(reservation.getSeatsReserved()).append("</td></tr>");
        sb.append("</table>");

        if (cov != null) {
            sb.append("<h3 style='color:#004d40;border-bottom:2px solid #e0e0e0;padding-bottom:8px;margin-top:20px;'>Trajet</h3>");
            sb.append("<table style='width:100%;border-collapse:collapse;'>");
            sb.append("<tr><td style='padding:6px 0;color:#666;width:40%;'>Conducteur :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getDriverName()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Trajet :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getDeparture()).append(" &rarr; ").append(cov.getDestination()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Date :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getDate()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Heure :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getHeureDepart() != null ? cov.getHeureDepart() : "--:--").append(" - ").append(cov.getHeureArrivee() != null ? cov.getHeureArrivee() : "--:--").append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Prix :</td><td style='padding:6px 0;font-weight:600;color:#2e7d32;'>").append(cov.getPrice()).append(" TND</td></tr>");
            sb.append("</table>");
        }

        sb.append("</div>");
        sb.append("<div style='background:#f8f9fa;padding:16px;text-align:center;color:#999;font-size:12px;'>");
        sb.append("<p style='margin:0;'>Cet email a ete envoye automatiquement - PIDEV Covoiturage</p>");
        sb.append("</div>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String buildEmailHtml(Reservation reservation, Covoiturage cov) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px;'>");
        sb.append("<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.1);'>");

        // Header
        sb.append("<div style='background:linear-gradient(135deg,#004d40,#00897b);color:#fff;padding:24px;text-align:center;'>");
        sb.append("<h1 style='margin:0;font-size:22px;'>Reservation Confirmee</h1>");
        sb.append("<p style='margin:8px 0 0;opacity:0.9;'>Reservation #").append(reservation.getId()).append("</p>");
        sb.append("</div>");

        // Body
        sb.append("<div style='padding:24px;'>");

        // Client info
        sb.append("<h3 style='color:#004d40;border-bottom:2px solid #e0e0e0;padding-bottom:8px;'>Informations Client</h3>");
        sb.append("<table style='width:100%;border-collapse:collapse;'>");
        sb.append("<tr><td style='padding:6px 0;color:#666;width:40%;'>Nom :</td><td style='padding:6px 0;font-weight:600;'>").append(reservation.getClientName()).append("</td></tr>");
        sb.append("<tr><td style='padding:6px 0;color:#666;'>Telephone :</td><td style='padding:6px 0;font-weight:600;'>").append(reservation.getPhone()).append("</td></tr>");
        sb.append("<tr><td style='padding:6px 0;color:#666;'>Places reservees :</td><td style='padding:6px 0;font-weight:600;'>").append(reservation.getSeatsReserved()).append("</td></tr>");
        sb.append("<tr><td style='padding:6px 0;color:#666;'>Date de reservation :</td><td style='padding:6px 0;font-weight:600;'>").append(reservation.getBookingDate()).append("</td></tr>");
        sb.append("</table>");

        // Covoiturage info
        if (cov != null) {
            sb.append("<h3 style='color:#004d40;border-bottom:2px solid #e0e0e0;padding-bottom:8px;margin-top:20px;'>Details du Trajet</h3>");
            sb.append("<table style='width:100%;border-collapse:collapse;'>");
            sb.append("<tr><td style='padding:6px 0;color:#666;width:40%;'>Conducteur :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getDriverName()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Depart :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getDeparture()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Destination :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getDestination()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Date du trajet :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getDate()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Heure depart :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getHeureDepart() != null ? cov.getHeureDepart() : "--:--").append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Heure arrivee :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getHeureArrivee() != null ? cov.getHeureArrivee() : "--:--").append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Vehicule :</td><td style='padding:6px 0;font-weight:600;'>").append(cov.getVehicle()).append("</td></tr>");
            sb.append("<tr><td style='padding:6px 0;color:#666;'>Prix :</td><td style='padding:6px 0;font-weight:600;color:#2e7d32;'>").append(cov.getPrice()).append(" TND</td></tr>");
            sb.append("</table>");
        }

        // Status badge
        sb.append("<div style='margin-top:20px;padding:14px;background:#fff3e0;border-radius:8px;text-align:center;'>");
        sb.append("<span style='color:#e65100;font-weight:700;font-size:16px;'>Statut : EN ATTENTE</span>");
        sb.append("<p style='color:#666;margin:6px 0 0;font-size:13px;'>Le conducteur va confirmer votre reservation.</p>");
        sb.append("</div>");

        sb.append("</div>");

        // Footer
        sb.append("<div style='background:#f8f9fa;padding:16px;text-align:center;color:#999;font-size:12px;'>");
        sb.append("<p style='margin:0;'>Cet email a ete envoye automatiquement - PIDEV Covoiturage</p>");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }
}

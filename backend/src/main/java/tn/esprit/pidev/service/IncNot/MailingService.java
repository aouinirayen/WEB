package tn.esprit.pidev.service.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;

import java.util.logging.Logger;

@Service
public class MailingService {

    private static final Logger logger = Logger.getLogger(MailingService.class.getName());

    @Autowired
    private JavaMailSender mailSender;

    // ✅ Email to AGENTS using DTO
    public void sendIncidentNotificationEmail(String toEmail, String agentName,
                                              IncidentNotificationDTO dto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("New Incident: " + dto.getTitle());
            message.setText(
                    "Hello " + agentName + ",\n\n" +
                            "A new incident has been reported:\n\n" +
                            "Title: "       + dto.getTitle() + "\n" +
                            "Severity: "    + dto.getSeverity() + "\n" +
                            "Location: "    + dto.getLocation() + "\n" +
                            "Reported by: " + dto.getReportedByName() + "\n\n" +
                            "Please log in to take action.\n\n" +
                            "Regards,\nPIDEV System"
            );
            mailSender.send(message);
            logger.info("Agent email sent to: " + toEmail);
        } catch (Exception e) {
            logger.severe("Failed to send agent email: " + e.getMessage());
        }
    }

    // ✅ Email to PASSENGERS using DTO
    public void sendTransportDelayEmail(String toEmail, String passengerName,
                                        IncidentNotificationDTO dto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Transport Delay Notice");
            message.setText(
                    "Dear " + passengerName + ",\n\n" +
                            "Your transport will be delayed due to an incident:\n\n" +
                            "Location: " + dto.getLocation() + "\n" +
                            "Severity: " + dto.getSeverity() + "\n" +
                            "Reason: "   + dto.getTitle() + "\n\n" +
                            "We apologize for the inconvenience.\n\n" +
                            "Regards,\nPIDEV Transport System"
            );
            mailSender.send(message);
            logger.info("Passenger email sent to: " + toEmail);
        } catch (Exception e) {
            logger.severe("Failed to send passenger email: " + e.getMessage());
        }
    }
}
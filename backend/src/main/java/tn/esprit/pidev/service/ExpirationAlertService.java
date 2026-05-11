package tn.esprit.pidev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExpirationAlertService {

    private static final Logger logger = LoggerFactory.getLogger(ExpirationAlertService.class);

    @Value("${n8n.expiration-alert.url:http://localhost:5678/webhook/expiration-alert}")
    private String n8nWebhookUrl;

    /**
     * Declenche automatiquement la verification des expirations chaque jour a 8h (heure Tunis).
     */
    @Scheduled(cron = "0 0 8 * * *", zone = "Africa/Tunis")
    public void scheduledExpirationCheck() {
        logger.info("[SCHEDULED] Triggering daily contract expiration check via n8n...");
        triggerAlert();
    }

    /**
     * Declenchement manuel (bouton admin).
     */
    public void triggerAlert() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            restTemplate.postForObject(n8nWebhookUrl, request, String.class);
            logger.info("n8n expiration-alert webhook triggered successfully at {}", n8nWebhookUrl);
        } catch (Exception ex) {
            logger.error("Failed to trigger n8n expiration-alert webhook: {}", ex.getMessage());
            throw new RuntimeException("Failed to trigger expiration alert: " + ex.getMessage(), ex);
        }
    }
}
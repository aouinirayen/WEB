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
public class WeeklyReportService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportService.class);

    @Value("${n8n.weekly-report.url:http://localhost:5678/webhook/weekly-report}")
    private String n8nWebhookUrl;

    @Scheduled(cron = "0 0 9 * * MON", zone = "Africa/Tunis")
    public void scheduledWeeklyReport() {
        logger.info("[SCHEDULED] Triggering weekly contract report via n8n...");
        triggerReport();
    }

    public void triggerReport() {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>("{}", headers);

            restTemplate.postForObject(n8nWebhookUrl, request, String.class);
            logger.info("n8n weekly report webhook triggered successfully at {}", n8nWebhookUrl);
        } catch (Exception ex) {
            logger.error("Failed to trigger n8n weekly report webhook: {}", ex.getMessage());
            throw new RuntimeException("Failed to trigger weekly report: " + ex.getMessage(), ex);
        }
    }
}
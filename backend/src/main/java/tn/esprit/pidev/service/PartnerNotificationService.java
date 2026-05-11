package tn.esprit.pidev.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.pidev.entity.Partner;

import java.util.HashMap;
import java.util.Map;

@Service
public class PartnerNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PartnerNotificationService.class);

    @Value("${n8n.new-partner.url:http://localhost:5678/webhook/new-partner}")
    private String n8nWebhookUrl;

    /**
     * Notifie n8n de la creation d'un nouveau partenaire (async, non bloquant).
     * Si l'appel echoue, on log l'erreur mais on ne casse pas la creation du partenaire.
     */
    @Async
    public void notifyNewPartner(Partner partner) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("partnerId", partner.getId());
            payload.put("partnerName", partner.getName());
            payload.put("industrySector", partner.getIndustrySector());
            payload.put("partnershipType", partner.getPartnershipType());
            payload.put("email", partner.getEmail());
            payload.put("phoneNumber", partner.getPhoneNumber());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(n8nWebhookUrl, request, String.class);
            logger.info("n8n new-partner webhook triggered for partner #{} ({})", partner.getId(), partner.getName());
        } catch (Exception ex) {
            logger.warn("Failed to trigger n8n new-partner webhook for partner #{}: {}", partner.getId(), ex.getMessage());
            // On ne propage pas l'erreur : la creation du partenaire doit rester OK meme si n8n est down
        }
    }
}
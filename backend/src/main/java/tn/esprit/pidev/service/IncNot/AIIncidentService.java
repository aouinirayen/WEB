package tn.esprit.pidev.service.IncNot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class AIIncidentService {

    private static final Logger logger = Logger.getLogger(AIIncidentService.class.getName());

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.incident.api.url:http://localhost:5000/predict}")
    private String aiApiUrl;

    public AIIncidentAnalysis analyzeIncident(String title, String description) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("title", title);
            body.put("description", description);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(aiApiUrl, request, Map.class);
            Map<String, Object> result = response.getBody();

            if (result != null) {
                return new AIIncidentAnalysis(
                        (String) result.getOrDefault("severity", "LOW"),
                        ((Number) result.getOrDefault("estimated_delay_minutes", 5)).intValue(),
                        (String) result.getOrDefault("agent_message", ""),
                        (String) result.getOrDefault("passenger_message", ""),
                        (String) result.getOrDefault("incident_type", "general"),
                        ((Number) result.getOrDefault("confidence_percent", 0.0)).doubleValue(),
                        (Boolean) result.getOrDefault("duplicate_likely", false),
                        ((Number) result.getOrDefault("duplicate_score_percent", 0.0)).doubleValue(),
                        (String) result.getOrDefault("similar_incident_title", "")
                );
            }
        } catch (Exception e) {
            logger.warning("AI API unavailable, fallback used: " + e.getMessage());
        }

        return fallbackAnalysis(title, description);
    }

    private AIIncidentAnalysis fallbackAnalysis(String title, String description) {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase();
        String severity;
        int delay;

        if (text.contains("fire") || text.contains("gas") || text.contains("collision")
                || text.contains("flood") || text.contains("critical") || text.contains("evacuate")) {
            severity = "HIGH";
            delay = 60;
        } else if (text.contains("delay") || text.contains("broken") || text.contains("medical")
                || text.contains("crowd") || text.contains("failure")) {
            severity = "MEDIUM";
            delay = 20;
        } else {
            severity = "LOW";
            delay = 5;
        }

        return new AIIncidentAnalysis(
                severity,
                delay,
                "Incident reported: " + title + ". Please investigate immediately.",
                "Transport disruption: " + title + ". Estimated delay: " + delay + " min.",
                "general",
                75.0,
                false,
                0.0,
                ""
        );
    }

    public static class AIIncidentAnalysis {
        private final String severity;
        private final int estimatedDelayMinutes;
        private final String agentMessage;
        private final String passengerMessage;
        private final String incidentType;
        private final double confidencePercent;
        private final boolean duplicateLikely;
        private final double duplicateScorePercent;
        private final String similarIncidentTitle;

        public AIIncidentAnalysis(String severity, int estimatedDelayMinutes,
                                  String agentMessage, String passengerMessage,
                                  String incidentType, double confidencePercent,
                                  boolean duplicateLikely, double duplicateScorePercent,
                                  String similarIncidentTitle) {
            this.severity = severity;
            this.estimatedDelayMinutes = estimatedDelayMinutes;
            this.agentMessage = agentMessage;
            this.passengerMessage = passengerMessage;
            this.incidentType = incidentType;
            this.confidencePercent = confidencePercent;
            this.duplicateLikely = duplicateLikely;
            this.duplicateScorePercent = duplicateScorePercent;
            this.similarIncidentTitle = similarIncidentTitle;
        }

        public String getSeverity() { return severity; }

        public int getEstimatedDelayMinutes() { return estimatedDelayMinutes; }

        public String getAgentMessage() { return agentMessage; }

        public String getPassengerMessage() { return passengerMessage; }

        public String getIncidentType() { return incidentType; }

        public double getConfidencePercent() { return confidencePercent; }

        public boolean getDuplicateLikely() { return duplicateLikely; }

        public double getDuplicateScorePercent() { return duplicateScorePercent; }

        public String getSimilarIncidentTitle() { return similarIncidentTitle; }
    }
}

package tn.esprit.pidev.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiVisionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiVisionService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String analyzeLicense(byte[] imageBytes,
                                 String mediaType) {
        String base64Image = Base64.getEncoder()
                .encodeToString(imageBytes);

        // Build request body for Gemini
        Map<String, Object> imagePart = new HashMap<>();
        Map<String, Object> inlineData = new HashMap<>();
        inlineData.put("mime_type", mediaType);
        inlineData.put("data", base64Image);
        imagePart.put("inline_data", inlineData);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text",
                "This is a driving license image. " +
                        "Extract the following information and return ONLY " +
                        "a JSON object with these exact fields: " +
                        "{\"firstName\": \"\", \"lastName\": \"\", " +
                        "\"licenseNumber\": \"\", \"licenseType\": \"\", " +
                        "\"expiryDate\": \"\", \"isValid\": true, " +
                        "\"validationNote\": \"\"}. " +
                        "For licenseType use one of: B, C, D, TC. " +
                        "If you cannot read the license clearly set isValid " +
                        "to false and explain in validationNote. " +
                        "Return ONLY the JSON, no markdown, no code blocks."
        );


        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(imagePart, textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        try {
            Map response = webClient.post()
                    .uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Parse Gemini response
            List<Map> candidates = (List<Map>)
                    response.get("candidates");
            Map firstCandidate = candidates.get(0);
            Map contentMap = (Map) firstCandidate.get("content");
            List<Map> parts = (List<Map>) contentMap.get("parts");
            String text = (String) parts.get(0).get("text");

            // Clean markdown if Gemini adds it
            text = text.replace("```json", "")
                    .replace("```", "")
                    .trim();

            return text;

        } catch (Exception e) {
            System.err.println("=== GEMINI ERROR ===");
            System.err.println(e.getMessage());
            return "{\"isValid\": false, \"validationNote\": \""
                    + e.getMessage().replace("\"", "'") + "\"}";
        }
    }
}
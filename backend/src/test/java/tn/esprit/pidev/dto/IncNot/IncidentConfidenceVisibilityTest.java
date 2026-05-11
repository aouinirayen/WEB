package tn.esprit.pidev.dto.IncNot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentConfidenceVisibilityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void publicIncidentDtoDoesNotExposeConfidence() throws Exception {
        IncidentNotificationDTO dto = new IncidentNotificationDTO(
                "Delay on line 2",
                "HIGH",
                "Central Station",
                "Agent A"
        );

        String json = objectMapper.writeValueAsString(dto);

        assertFalse(json.contains("confidencePercent"));
        assertFalse(json.contains("confidence"));
    }

    @Test
    void submissionResponseDtoExposesConfidenceToAgentOnly() throws Exception {
        IncidentSubmissionResponseDTO dto = new IncidentSubmissionResponseDTO(
                "Delay on line 2",
                "HIGH",
                "Central Station",
                "Agent A",
                87.5
        );

        String json = objectMapper.writeValueAsString(dto);

        assertTrue(json.contains("confidencePercent"));
        assertTrue(json.contains("87.5"));
    }
}


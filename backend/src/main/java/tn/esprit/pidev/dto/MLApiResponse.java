package tn.esprit.pidev.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MLApiResponse {

    private ChurnResult    churn;
    private ClvResult      clv;
    private RecommendResult recommendation;
    private String         action;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChurnResult {
        private double probability;
        private int    label;
        private String riskLevel;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClvResult {
        private double value;
        private String currency;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendResult {
        private String plan;
        private double confidence;
    }
}
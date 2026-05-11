package tn.esprit.pidev.ai.dto;

import java.util.Map;

public class AIStatsResponse {

    private Map<String, Object> matchingModel;
    private Map<String, Object> cancellationModel;
    private Map<String, Object> satisfactionModel;
    private long totalTrainingData;
    private String status;

    public AIStatsResponse() {}

    public Map<String, Object> getMatchingModel() { return matchingModel; }
    public void setMatchingModel(Map<String, Object> matchingModel) { this.matchingModel = matchingModel; }

    public Map<String, Object> getCancellationModel() { return cancellationModel; }
    public void setCancellationModel(Map<String, Object> cancellationModel) { this.cancellationModel = cancellationModel; }

    public Map<String, Object> getSatisfactionModel() { return satisfactionModel; }
    public void setSatisfactionModel(Map<String, Object> satisfactionModel) { this.satisfactionModel = satisfactionModel; }

    public long getTotalTrainingData() { return totalTrainingData; }
    public void setTotalTrainingData(long totalTrainingData) { this.totalTrainingData = totalTrainingData; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

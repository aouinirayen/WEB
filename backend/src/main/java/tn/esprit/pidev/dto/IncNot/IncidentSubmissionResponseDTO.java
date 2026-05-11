package tn.esprit.pidev.dto.IncNot;

public class IncidentSubmissionResponseDTO extends IncidentNotificationDTO {

    private double confidencePercent;

    public IncidentSubmissionResponseDTO() {
        super();
    }

    public IncidentSubmissionResponseDTO(String title, String severity,
                                         String location, String reportedByName,
                                         double confidencePercent) {
        super(title, severity, location, reportedByName);
        this.confidencePercent = confidencePercent;
    }

    public double getConfidencePercent() {
        return confidencePercent;
    }

    public void setConfidencePercent(double confidencePercent) {
        this.confidencePercent = confidencePercent;
    }
}


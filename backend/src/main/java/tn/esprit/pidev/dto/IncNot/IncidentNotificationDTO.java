package tn.esprit.pidev.dto.IncNot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class IncidentNotificationDTO {
    @NotBlank(message = "Le titre de l'incident est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères")
    private String title;

    @NotNull(message = "La sévérité est obligatoire")
    private String severity; // ou String si c'est un enum

    @NotBlank(message = "La localisation est obligatoire")
    @Size(max = 255, message = "La localisation ne peut pas dépasser 255 caractères")
    private String location;

    @Size(max = 100, message = "Le nom du déclarant ne peut pas dépasser 100 caractères")
    private String reportedByName;

    public IncidentNotificationDTO() {}

    public IncidentNotificationDTO(String title, String severity,
                                   String location, String reportedByName) {
        this.title = title;
        this.severity = severity;
        this.location = location;
        this.reportedByName = reportedByName;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }
}
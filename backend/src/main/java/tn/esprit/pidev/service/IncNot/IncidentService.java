package tn.esprit.pidev.service.IncNot;

import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.dto.IncNot.IncidentSubmissionResponseDTO;
import tn.esprit.pidev.entity.Incident;
import java.util.List;

public interface IncidentService {
    IncidentSubmissionResponseDTO saveIncident(Incident incident, String agentUsername);
    void deleteIncident(Long id);
    IncidentNotificationDTO getIncidentById(Long id);
    List<IncidentNotificationDTO> getAllIncidents();
}
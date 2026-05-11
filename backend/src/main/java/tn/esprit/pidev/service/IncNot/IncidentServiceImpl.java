package tn.esprit.pidev.service.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.dto.IncNot.IncidentSubmissionResponseDTO;
import tn.esprit.pidev.entity.Incident;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.exception.InvalidFileException;
import tn.esprit.pidev.repository.IncidentRepository;
import tn.esprit.pidev.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncidentServiceImpl implements IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AIIncidentService aiIncidentService;

    // ✅ converter — Incident entity → IncidentNotificationDTO
    private IncidentNotificationDTO convertToDTO(Incident incident) {
        return new IncidentNotificationDTO(
                incident.getTitle(),
                incident.getSeverity(),
                incident.getLocation(),
                incident.getReportedBy() != null ? incident.getReportedBy().getName() : null
        );
    }

    @Override
    public IncidentSubmissionResponseDTO saveIncident(Incident incident, String agentUsername) {
        User agent = userRepository.findByUsername(agentUsername)
                .orElseThrow(() -> new InvalidFileException("User not found: " + agentUsername));

        if (agent.getRole() != RoleEnum.AGENT) {
            throw new InvalidFileException("Only users with role AGENT can submit incidents");
        }

        incident.setReportedBy(agent);
        Incident savedIncident = incidentRepository.save(incident);

        AIIncidentService.AIIncidentAnalysis analysis = aiIncidentService.analyzeIncident(
                savedIncident.getTitle(),
                savedIncident.getDescription()
        );

        // ✅ Build DTO with only important fields
        IncidentNotificationDTO dto = new IncidentNotificationDTO(
                savedIncident.getTitle(),
                savedIncident.getSeverity(),
                savedIncident.getLocation(),
                agent.getName()
        );

        // ✅ Pass DTO to notification service
        notificationService.sendInternalNotificationsToAgents(dto);
        notificationService.sendExternalNotificationsToAgents(dto);
        notificationService.sendDelayNotificationsToPassengers(dto);

        return new IncidentSubmissionResponseDTO(
                savedIncident.getTitle(),
                savedIncident.getSeverity(),
                savedIncident.getLocation(),
                agent.getName(),
                analysis.getConfidencePercent()
        );
    }

    @Override
    public void deleteIncident(Long id) {
        incidentRepository.deleteById(id);
    }

    @Override
    public IncidentNotificationDTO getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id).orElse(null);
        return incident != null ? convertToDTO(incident) : null;
    }

    @Override
    public List<IncidentNotificationDTO> getAllIncidents() {
        return incidentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
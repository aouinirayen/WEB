package tn.esprit.pidev.controller.IncNot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pidev.dto.IncNot.IncidentNotificationDTO;
import tn.esprit.pidev.dto.IncNot.IncidentSubmissionResponseDTO;
import tn.esprit.pidev.entity.Incident;
import tn.esprit.pidev.service.IncNot.IncidentService;

import java.util.List;

@RestController
@RequestMapping("/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @PostMapping("/add")
    public IncidentSubmissionResponseDTO createIncident(@RequestBody Incident incident,
                                                        Authentication authentication) {
        return incidentService.saveIncident(incident, authentication.getName());
    }

    @PutMapping("/update/{id}")
    public IncidentSubmissionResponseDTO updateIncident(@PathVariable Long id,
                                                        @RequestBody Incident incident,
                                                        Authentication authentication) {
        incident.setId(id);
        return incidentService.saveIncident(incident, authentication.getName());
    }

    @DeleteMapping("/delete/{id}")
    public void deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
    }

    @GetMapping("/get/{id}")
    public IncidentNotificationDTO getIncidentById(@PathVariable Long id) {
        return incidentService.getIncidentById(id);
    }

    @GetMapping
    public List<IncidentNotificationDTO> getAllIncidents() {
        return incidentService.getAllIncidents();
    }
}
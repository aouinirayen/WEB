package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.OrganizationDTO;
import tn.esprit.pidev.enums.CoverageType;
import tn.esprit.pidev.enums.OrgStatus;
import java.util.List;

public interface OrganizationService {
    OrganizationDTO create(OrganizationDTO dto);
    OrganizationDTO update(Long id, OrganizationDTO dto);
    void delete(Long id);
    OrganizationDTO getById(Long id);
    List<OrganizationDTO> getAll();
    List<OrganizationDTO> getByStatus(OrgStatus status);
    List<OrganizationDTO> getByCoverageType(CoverageType coverageType);
}

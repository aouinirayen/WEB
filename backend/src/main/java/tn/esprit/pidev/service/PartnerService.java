package tn.esprit.pidev.service;
import tn.esprit.pidev.dto.PartnerDTO;
import tn.esprit.pidev.enums.PartnerStatus;
import java.util.List;
public interface PartnerService {
    PartnerDTO create(PartnerDTO dto);
    PartnerDTO update(Long id, PartnerDTO dto);
    void delete(Long id);
    PartnerDTO getById(Long id);
    List<PartnerDTO> getAll();
    List<PartnerDTO> getByStatus(PartnerStatus status);
    List<PartnerDTO> getByOrganizationId(Long organizationId);
}

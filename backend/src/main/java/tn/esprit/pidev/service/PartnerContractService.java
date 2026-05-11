package tn.esprit.pidev.service;

import tn.esprit.pidev.dto.PartnerContractDTO;
import tn.esprit.pidev.enums.ContractStatus;
import java.util.List;

public interface PartnerContractService {
    PartnerContractDTO create(PartnerContractDTO dto);
    PartnerContractDTO update(Long id, PartnerContractDTO dto);
    void delete(Long id);
    PartnerContractDTO getById(Long id);
    List<PartnerContractDTO> getAll();
    List<PartnerContractDTO> getByOrganizationId(Long organizationId);
    List<PartnerContractDTO> getByPartnerId(Long partnerId);
    List<PartnerContractDTO> getByStatus(ContractStatus status);
}

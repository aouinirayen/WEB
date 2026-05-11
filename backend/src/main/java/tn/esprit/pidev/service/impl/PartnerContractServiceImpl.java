package tn.esprit.pidev.service.impl;

import tn.esprit.pidev.dto.PartnerContractDTO;
import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.entity.Partner;
import tn.esprit.pidev.enums.ContractStatus;
import tn.esprit.pidev.repository.*;
import tn.esprit.pidev.service.PartnerContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerContractServiceImpl implements PartnerContractService {

    private final PartnerContractRepository contractRepository;
    private final OrganizationRepository organizationRepository;
    private final PartnerRepository partnerRepository;

    private PartnerContractDTO toDTO(PartnerContract contract) {
        return PartnerContractDTO.builder()
                .id(contract.getId())
                .contractType(contract.getContractType())
                .status(contract.getStatus())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .description(contract.getDescription())
                .organizationId(contract.getOrganization() != null ? contract.getOrganization().getId() : null)
                .organizationName(contract.getOrganization() != null ? contract.getOrganization().getName() : "-")
                .partnerId(contract.getPartner() != null ? contract.getPartner().getId() : null)
                .partnerName(contract.getPartner() != null ? contract.getPartner().getName() : "-")
                .isSigned(contract.getIsSigned())
                .signatureValid(contract.getSignatureValid())
                .signedBy(contract.getSignedBy())
                .signedAt(contract.getSignedAt())
                .signatureHash(contract.getSignatureHash())
                .contentHash(contract.getContentHash())
                .build();
    }

    private PartnerContract toEntity(PartnerContractDTO dto) {
        Organization org = organizationRepository.findById(dto.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));
        Partner partner = partnerRepository.findById(dto.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Partner not found"));
        return PartnerContract.builder()
                .contractType(dto.getContractType())
                .status(dto.getStatus())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .description(dto.getDescription())
                .organization(org)
                .partner(partner)
                .build();
    }

    @Override
    public PartnerContractDTO create(PartnerContractDTO dto) {
        return toDTO(contractRepository.save(toEntity(dto)));
    }

    @Override
    public PartnerContractDTO update(Long id, PartnerContractDTO dto) {
        PartnerContract existing = contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
        existing.setContractType(dto.getContractType());
        existing.setStatus(dto.getStatus());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setDescription(dto.getDescription());
        return toDTO(contractRepository.save(existing));
    }

    @Override
    public void delete(Long id) { contractRepository.deleteById(id); }

    @Override
    public PartnerContractDTO getById(Long id) {
        return contractRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Contract not found"));
    }

    @Override
    public List<PartnerContractDTO> getAll() {
        return contractRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PartnerContractDTO> getByOrganizationId(Long organizationId) {
        return contractRepository.findByOrganizationId(organizationId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PartnerContractDTO> getByPartnerId(Long partnerId) {
        return contractRepository.findByPartnerId(partnerId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PartnerContractDTO> getByStatus(ContractStatus status) {
        return contractRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }
}

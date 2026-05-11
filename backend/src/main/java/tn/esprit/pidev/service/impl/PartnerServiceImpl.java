package tn.esprit.pidev.service.impl;
import tn.esprit.pidev.dto.PartnerDTO;
import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.entity.Partner;
import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.enums.ContractStatus;
import tn.esprit.pidev.enums.ContractType;
import tn.esprit.pidev.enums.PartnerStatus;
import tn.esprit.pidev.repository.OrganizationRepository;
import tn.esprit.pidev.repository.PartnerContractRepository;
import tn.esprit.pidev.repository.PartnerRepository;
import tn.esprit.pidev.service.PartnerService;
import tn.esprit.pidev.service.PartnerNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerContractRepository contractRepository;
    private final OrganizationRepository organizationRepository;
    private final PartnerNotificationService partnerNotificationService;

    private PartnerDTO toDTO(Partner partner) {
        return PartnerDTO.builder()
                .id(partner.getId())
                .name(partner.getName())
                .industrySector(partner.getIndustrySector())
                .partnershipType(partner.getPartnershipType())
                .email(partner.getEmail())
                .phoneNumber(partner.getPhoneNumber())
                .website(partner.getWebsite())
                .logo(partner.getLogo())
                .status(partner.getStatus())
                .build();
    }

    private Partner toEntity(PartnerDTO dto) {
        return Partner.builder()
                .id(dto.getId())
                .name(dto.getName())
                .industrySector(dto.getIndustrySector())
                .partnershipType(dto.getPartnershipType())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .website(dto.getWebsite())
                .logo(dto.getLogo())
                .status(dto.getStatus())
                .build();
    }

    @Override
    public PartnerDTO create(PartnerDTO dto) {
        Partner saved = partnerRepository.save(toEntity(dto));

        Organization organization = null;
        if (dto.getOrganizationId() != null) {
            organization = organizationRepository.findById(dto.getOrganizationId()).orElse(null);
        }

        // FIX: start date = demain (pour eviter le bug @FutureOrPresent avec millisecondes)
        long ONE_DAY_MS = 24L * 60 * 60 * 1000;
        Date startDate = new Date(System.currentTimeMillis() + ONE_DAY_MS);
        Date endDate = new Date(System.currentTimeMillis() + 365L * ONE_DAY_MS);

        PartnerContract contract = PartnerContract.builder()
                .contractType(ContractType.COMMERCIAL)
                .status(ContractStatus.DRAFT)
                .startDate(startDate)
                .endDate(endDate)
                .description("Auto-generated contract for partner: " + saved.getName())
                .partner(saved)
                .organization(organization)
                .build();
        contractRepository.save(contract);

        // Notifier n8n (async, non bloquant)
        partnerNotificationService.notifyNewPartner(saved);

        return toDTO(saved);
    }

    @Override
    public PartnerDTO update(Long id, PartnerDTO dto) {
        Partner existing = partnerRepository.findById(id).orElseThrow(() -> new RuntimeException("Partner not found"));
        existing.setName(dto.getName());
        existing.setIndustrySector(dto.getIndustrySector());
        existing.setPartnershipType(dto.getPartnershipType());
        existing.setEmail(dto.getEmail());
        existing.setPhoneNumber(dto.getPhoneNumber());
        existing.setWebsite(dto.getWebsite());
        existing.setLogo(dto.getLogo());
        existing.setStatus(dto.getStatus());
        return toDTO(partnerRepository.save(existing));
    }

    @Override
    public void delete(Long id) { partnerRepository.deleteById(id); }

    @Override
    public PartnerDTO getById(Long id) {
        return partnerRepository.findById(id).map(this::toDTO).orElseThrow(() -> new RuntimeException("Partner not found"));
    }

    @Override
    public List<PartnerDTO> getAll() {
        return partnerRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PartnerDTO> getByStatus(PartnerStatus status) {
        return partnerRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PartnerDTO> getByOrganizationId(Long organizationId) {
        return contractRepository.findByOrganizationId(organizationId).stream().map(c -> toDTO(c.getPartner())).collect(Collectors.toList());
    }
}
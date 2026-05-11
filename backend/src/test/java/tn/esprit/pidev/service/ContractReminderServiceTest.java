package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.entity.Partner;
import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.enums.ContractStatus;
import tn.esprit.pidev.enums.ContractType;
import tn.esprit.pidev.repository.PartnerContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests - Systeme de Rappel de Contrats")
class ContractReminderServiceTest {

    @Mock
    private PartnerContractRepository contractRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private tn.esprit.pidev.service.admin.LegalDocumentService legalDocumentService;

    @InjectMocks
    private ScheduledJobService scheduledJobService;

    private Partner partner;
    private Organization organization;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        organization = new Organization();
        organization.setId(1L);
        organization.setName("SNTRI");
        partner = new Partner();
        partner.setId(1L);
        partner.setName("SICAME");
        partner.setEmail("aouinirayen44@gmail.com");
    }

    private PartnerContract createContract(int daysUntilExpiry) {
        Date endDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysUntilExpiry));
        return PartnerContract.builder()
            .id((long)(Math.random() * 1000 + 1))
            .contractType(ContractType.COMMERCIAL)
            .status(ContractStatus.ACTIVE)
            .startDate(new Date())
            .endDate(endDate)
            .description("Contrat test")
            .organization(organization)
            .partner(partner)
            .isSigned(false)
            .build();
    }

    @Test
    @DisplayName("Test 1 - Contrat expirant dans 3 jours declenche rappel")
    void testContractExpiringIn3Days() {
        when(contractRepository.findAll()).thenReturn(List.of(createContract(3)));
        scheduledJobService.checkExpiringContractsJob();
        verify(emailService, atLeastOnce()).sendContractExpiryReminderEmail(
            anyString(), anyString(), anyString(), any(), anyInt(), any(Date.class)
        );
        System.out.println("Rappel critique envoye - OK");
    }

    @Test
    @DisplayName("Test 2 - Contrat expirant dans 30 jours declenche rappel")
    void testContractExpiringIn30Days() {
        when(contractRepository.findAll()).thenReturn(List.of(createContract(30)));
        scheduledJobService.checkExpiringContractsJob();
        verify(emailService, atLeastOnce()).sendContractExpiryReminderEmail(
            anyString(), anyString(), anyString(), any(), anyInt(), any(Date.class)
        );
        System.out.println("Rappel normal envoye - OK");
    }

    @Test
    @DisplayName("Test 3 - Contrat expirant dans 10 jours ne declenche pas de rappel")
    void testContractExpiringIn10DaysNoReminder() {
        when(contractRepository.findAll()).thenReturn(List.of(createContract(10)));
        scheduledJobService.checkExpiringContractsJob();
        verify(emailService, never()).sendContractExpiryReminderEmail(
            anyString(), anyString(), anyString(), any(), anyInt(), any(Date.class)
        );
        System.out.println("Aucun rappel pour 10 jours - OK");
    }

    @Test
    @DisplayName("Test 4 - Contrat sans email ne declenche pas d envoi")
    void testContractWithNoEmailNoSend() {
        partner.setEmail(null);
        when(contractRepository.findAll()).thenReturn(List.of(createContract(3)));
        scheduledJobService.checkExpiringContractsJob();
        verify(emailService, never()).sendContractExpiryReminderEmail(
            anyString(), anyString(), anyString(), any(), anyInt(), any(Date.class)
        );
        System.out.println("Aucun email sans adresse - OK");
    }

    @Test
    @DisplayName("Test 5 - Liste vide ne declenche aucun rappel")
    void testEmptyContractListNoReminders() {
        when(contractRepository.findAll()).thenReturn(List.of());
        scheduledJobService.checkExpiringContractsJob();
        verify(emailService, never()).sendContractExpiryReminderEmail(
            anyString(), anyString(), anyString(), any(), anyInt(), any(Date.class)
        );
        System.out.println("Aucun rappel pour liste vide - OK");
    }
}


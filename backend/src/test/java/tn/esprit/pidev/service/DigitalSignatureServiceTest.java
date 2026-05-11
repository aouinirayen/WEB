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
import java.util.Date;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Tests - Signature Numerique et Detection de Fraude")
class DigitalSignatureServiceTest {

    @Mock
    private PartnerContractRepository contractRepository;

    @InjectMocks
    private DigitalSignatureService signatureService;

    private PartnerContract contract;
    private Organization organization;
    private Partner partner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        organization = new Organization();
        organization.setId(1L);
        organization.setName("SNTRI");

        partner = new Partner();
        partner.setId(1L);
        partner.setName("SICAME");
        partner.setEmail("sicame@gmail.com");

        contract = PartnerContract.builder()
            .id(1L)
            .contractType(ContractType.COMMERCIAL)
            .status(ContractStatus.ACTIVE)
            .startDate(new Date())
            .endDate(new Date())
            .description("Contrat de partenariat TransitTN")
            .organization(organization)
            .partner(partner)
            .isSigned(false)
            .signatureValid(false)
            .build();
    }

    @Test
    @DisplayName("Test 1 - Generation du hash SHA-256")
    void testGenerateContentHash() {
        String hash = signatureService.generateContentHash(contract);
        assertNotNull(hash, "Le hash ne doit pas etre null");
        assertEquals(64, hash.length(), "Le hash SHA-256 doit avoir 64 caracteres");
        assertTrue(hash.matches("[a-f0-9]+"), "Le hash doit etre en hexadecimal");
        System.out.println("Hash genere: " + hash);
    }

    @Test
    @DisplayName("Test 2 - Hash different pour contenu different")
    void testDifferentHashForDifferentContent() {
        String hash1 = signatureService.generateContentHash(contract);
        contract.setDescription("Description modifiee");
        String hash2 = signatureService.generateContentHash(contract);
        assertNotEquals(hash1, hash2, "Deux contenus differents doivent avoir des hashes differents");
        System.out.println("Hash original: " + hash1);
        System.out.println("Hash modifie: " + hash2);
    }

    @Test
    @DisplayName("Test 3 - Signature d'un contrat")
    void testSignContract() {
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(PartnerContract.class))).thenAnswer(i -> i.getArguments()[0]);

        PartnerContract signed = signatureService.signContract(1L, "Admin");

        assertTrue(signed.getIsSigned(), "Le contrat doit etre marque comme signe");
        assertTrue(signed.getSignatureValid(), "La signature doit etre valide");
        assertNotNull(signed.getSignatureHash(), "Le hash de signature ne doit pas etre null");
        assertNotNull(signed.getContentHash(), "Le hash du contenu ne doit pas etre null");
        assertEquals("Admin", signed.getSignedBy(), "Le signataire doit etre Admin");
        assertNotNull(signed.getSignedAt(), "La date de signature ne doit pas etre null");
        System.out.println("Contrat signe avec hash: " + signed.getSignatureHash());
    }

    @Test
    @DisplayName("Test 4 - Impossibilite de signer un contrat deja signe")
    void testCannotSignAlreadySignedContract() {
        contract.setIsSigned(true);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        assertThrows(RuntimeException.class, 
            () -> signatureService.signContract(1L, "Admin"),
            "Une exception doit etre levee si le contrat est deja signe"
        );
        System.out.println("Exception correctement levee pour contrat deja signe");
    }

    @Test
    @DisplayName("Test 5 - Verification signature valide (pas de fraude)")
    void testVerifyValidSignature() {
        // Signer d'abord
        String contentHash = signatureService.generateContentHash(contract);
        contract.setContentHash(contentHash);
        contract.setSignatureHash("dummy_hash");
        contract.setIsSigned(true);
        contract.setSignatureValid(true);
        contract.setSignedBy("Admin");

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DigitalSignatureService.FraudCheckResult result = signatureService.verifySignature(1L);

        assertFalse(result.fraudDetected(), "Aucune fraude ne doit etre detectee");
        assertTrue(result.signatureValid(), "La signature doit etre valide");
        System.out.println("Resultat: " + result.message());
    }

    @Test
    @DisplayName("Test 6 - Detection de fraude apres modification")
    void testFraudDetectionAfterModification() {
        // Hash original
        String originalHash = signatureService.generateContentHash(contract);
        contract.setContentHash(originalHash);
        contract.setSignatureHash("dummy_hash");
        contract.setIsSigned(true);
        contract.setSignatureValid(true);

        // Modifier le contenu APRES signature (simulation fraude)
        contract.setDescription("Description modifiee frauduleusement !");

        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        DigitalSignatureService.FraudCheckResult result = signatureService.verifySignature(1L);

        assertTrue(result.fraudDetected(), "La fraude doit etre detectee !");
        assertFalse(result.signatureValid(), "La signature doit etre invalide");
        System.out.println("FRAUDE DETECTEE: " + result.message());
    }

    @Test
    @DisplayName("Test 7 - Verification contrat non signe")
    void testVerifyUnsignedContract() {
        contract.setIsSigned(false);
        when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));

        DigitalSignatureService.FraudCheckResult result = signatureService.verifySignature(1L);

        assertFalse(result.fraudDetected(), "Pas de fraude pour contrat non signe");
        assertFalse(result.signatureValid(), "Signature invalide pour contrat non signe");
        System.out.println("Resultat contrat non signe: " + result.message());
    }
}

package tn.esprit.pidev.controller;

import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.entity.Partner;
import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.repository.PartnerContractRepository;
import tn.esprit.pidev.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PdfController {

    private final PartnerRepository partnerRepository;
    private final PartnerContractRepository contractRepository;

    /**
     * Generate PDF for a SPECIFIC contract (not just partner's first contract).
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<byte[]> generateContractPdf(@PathVariable Long contractId) {
        PartnerContract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        Partner partner = contract.getPartner();
        Organization organization = contract.getOrganization();

        String html = buildContractHtml(partner, contract, organization);

        String filename = "Contract_" + safeName(partner != null ? partner.getName() : "partner")
                + "_" + safeName(organization != null ? organization.getName() : "org")
                + "_" + contractId + ".html";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.set("Content-Disposition", "inline; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(html.getBytes(StandardCharsets.UTF_8));
    }

    private String safeName(String name) {
        if (name == null) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String buildContractHtml(Partner partner, PartnerContract contract, Organization org) {
        String contractType = contract != null ? contract.getContractType().toString() : "N/A";
        String status = contract != null ? contract.getStatus().toString() : "N/A";
        String startDate = contract != null && contract.getStartDate() != null ? contract.getStartDate().toString() : "N/A";
        String endDate = contract != null && contract.getEndDate() != null ? contract.getEndDate().toString() : "N/A";
        String description = contract != null && contract.getDescription() != null ? contract.getDescription() : "Aucune description fournie.";

        String orgName = org != null ? org.getName() : "N/A";
        String orgEmail = org != null ? org.getEmail() : "N/A";
        String orgPhone = org != null ? org.getPhoneNumber() : "N/A";
        String orgType = org != null && org.getType() != null ? org.getType().toString() : "N/A";

        String partnerName = partner != null ? partner.getName() : "N/A";
        String partnerSector = partner != null ? partner.getIndustrySector() : "N/A";
        String partnerEmail = partner != null ? partner.getEmail() : "N/A";
        String partnerPhone = partner != null ? partner.getPhoneNumber() : "N/A";
        String partnershipType = partner != null ? partner.getPartnershipType() : "N/A";

        // Signature section
        boolean isSigned = contract != null && Boolean.TRUE.equals(contract.getIsSigned());
        String signedBy = isSigned ? contract.getSignedBy() : "Non signe";
        String signedAt = isSigned && contract.getSignedAt() != null ? contract.getSignedAt().toString() : "-";
        String contentHash = isSigned && contract.getContentHash() != null ? contract.getContentHash() : "-";
        String signatureHash = isSigned && contract.getSignatureHash() != null ? contract.getSignatureHash() : "-";
        String signatureBadge = isSigned
                ? "<span style='background:#e8f5e9; color:#2e7d32; padding:6px 14px; border-radius:20px; font-weight:bold;'>&#10003; SIGNE NUMERIQUEMENT</span>"
                : "<span style='background:#fff3e0; color:#e65100; padding:6px 14px; border-radius:20px; font-weight:bold;'>&#9888; NON SIGNE</span>";

        return "<!DOCTYPE html>"
            + "<html><head><meta charset='UTF-8'>"
            + "<title>Contrat TransitTN #" + (contract != null ? contract.getId() : "N/A") + "</title>"
            + "<style>"
            + "body { font-family: 'Segoe UI', Arial, sans-serif; margin: 40px; color: #333; background: #f5f7fa; }"
            + ".container { max-width: 900px; margin: auto; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }"
            + ".header { text-align: center; border-bottom: 3px solid #1a237e; padding-bottom: 20px; margin-bottom: 30px; }"
            + ".logo { font-size: 2.2rem; font-weight: bold; color: #1a237e; letter-spacing: 2px; }"
            + ".title { font-size: 1.4rem; color: #1a73e8; margin: 10px 0; font-weight: 600; }"
            + ".subtitle { color: #666; }"
            + ".section { margin: 20px 0; padding: 18px; background: #f8f9ff; border-radius: 8px; border-left: 4px solid #1a73e8; }"
            + ".section h3 { color: #1a237e; margin: 0 0 12px; font-size: 1.1rem; }"
            + ".row { display: flex; justify-content: space-between; margin: 8px 0; padding: 4px 0; border-bottom: 1px dashed #e0e0e0; }"
            + ".row:last-child { border-bottom: none; }"
            + ".label { font-weight: 600; color: #555; }"
            + ".value { color: #222; }"
            + ".badge { background: #e8f5e9; color: #2e7d32; padding: 4px 12px; border-radius: 20px; font-weight: bold; font-size: 0.85rem; }"
            + ".parties { display: flex; gap: 20px; margin: 20px 0; }"
            + ".party-box { flex: 1; padding: 18px; border-radius: 8px; border-left: 4px solid #1a73e8; background: #f8f9ff; }"
            + ".party-box h3 { color: #1a237e; margin: 0 0 10px; font-size: 1rem; }"
            + ".signature-section { margin-top: 40px; background: #fff8e1; border: 2px dashed #ffa726; padding: 20px; border-radius: 10px; }"
            + ".signature-section h3 { color: #e65100; margin: 0 0 15px; }"
            + ".hash-box { font-family: 'Consolas', monospace; background: #263238; color: #aed581; padding: 10px; border-radius: 6px; font-size: 0.75rem; margin-top: 5px; word-break: break-all; }"
            + ".footer { margin-top: 40px; text-align: center; color: #999; font-size: 0.85rem; border-top: 1px solid #eee; padding-top: 20px; }"
            + ".signatures { margin-top: 40px; display: flex; justify-content: space-between; }"
            + ".sig-box { text-align: center; width: 200px; }"
            + ".sig-line { border-top: 2px solid #333; margin-top: 50px; padding-top: 10px; font-weight: 600; }"
            + "</style></head>"
            + "<body><div class='container'>"
            + "<div class='header'>"
            + "  <div class='logo'>TransitTN</div>"
            + "  <div class='title'>CONTRAT DE PARTENARIAT</div>"
            + "  <div class='subtitle'>Plateforme Tunisienne de Transport Public</div>"
            + "  <div style='margin-top:15px;'>" + signatureBadge + "</div>"
            + "</div>"
            + "<div class='section'>"
            + "  <h3>Informations du Contrat</h3>"
            + "  <div class='row'><span class='label'>ID Contrat :</span><span class='value'>#" + (contract != null ? contract.getId() : "N/A") + "</span></div>"
            + "  <div class='row'><span class='label'>Type :</span><span class='value'>" + contractType + "</span></div>"
            + "  <div class='row'><span class='label'>Statut :</span><span class='badge'>" + status + "</span></div>"
            + "  <div class='row'><span class='label'>Date debut :</span><span class='value'>" + startDate + "</span></div>"
            + "  <div class='row'><span class='label'>Date fin :</span><span class='value'>" + endDate + "</span></div>"
            + "</div>"
            + "<div class='parties'>"
            + "  <div class='party-box'>"
            + "    <h3>Partie 1 - Organisation</h3>"
            + "    <div class='row'><span class='label'>Nom :</span><span class='value'>" + orgName + "</span></div>"
            + "    <div class='row'><span class='label'>Email :</span><span class='value'>" + orgEmail + "</span></div>"
            + "    <div class='row'><span class='label'>Telephone :</span><span class='value'>" + orgPhone + "</span></div>"
            + "    <div class='row'><span class='label'>Type :</span><span class='value'>" + orgType + "</span></div>"
            + "  </div>"
            + "  <div class='party-box'>"
            + "    <h3>Partie 2 - Partenaire</h3>"
            + "    <div class='row'><span class='label'>Nom :</span><span class='value'>" + partnerName + "</span></div>"
            + "    <div class='row'><span class='label'>Secteur :</span><span class='value'>" + partnerSector + "</span></div>"
            + "    <div class='row'><span class='label'>Email :</span><span class='value'>" + partnerEmail + "</span></div>"
            + "    <div class='row'><span class='label'>Telephone :</span><span class='value'>" + partnerPhone + "</span></div>"
            + "    <div class='row'><span class='label'>Partenariat :</span><span class='value'>" + partnershipType + "</span></div>"
            + "  </div>"
            + "</div>"
            + "<div class='section'>"
            + "  <h3>Description detaillee</h3>"
            + "  <div style='white-space:pre-wrap; line-height:1.6;'>" + description + "</div>"
            + "</div>"
            + "<div class='signature-section'>"
            + "  <h3>&#128274; Signature Numerique SHA-256</h3>"
            + "  <div class='row'><span class='label'>Signe par :</span><span class='value'>" + signedBy + "</span></div>"
            + "  <div class='row'><span class='label'>Date & Heure :</span><span class='value'>" + signedAt + "</span></div>"
            + "  <div><span class='label'>Hash du contenu :</span><div class='hash-box'>" + contentHash + "</div></div>"
            + "  <div style='margin-top:10px;'><span class='label'>Hash de signature :</span><div class='hash-box'>" + signatureHash + "</div></div>"
            + "</div>"
            + "<div class='signatures'>"
            + "  <div class='sig-box'><div class='sig-line'>Partenaire</div><div>" + partnerName + "</div></div>"
            + "  <div class='sig-box'><div class='sig-line'>Organisation</div><div>" + orgName + "</div></div>"
            + "  <div class='sig-box'><div class='sig-line'>TransitTN</div><div>Plateforme officielle</div></div>"
            + "</div>"
            + "<div class='footer'>"
            + "  <p>Document genere par TransitTN le " + java.time.LocalDate.now() + "</p>"
            + "  <p>Systeme de gestion du transport public tunisien</p>"
            + "</div>"
            + "</div></body></html>";
    }
}
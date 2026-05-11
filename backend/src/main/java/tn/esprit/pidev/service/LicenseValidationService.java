package tn.esprit.pidev.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.pidev.entity.Driver;
import tn.esprit.pidev.entity.enums.LicenseValidationStatus;
import tn.esprit.pidev.repository.DriverRepository;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class LicenseValidationService {

    private final DriverRepository driverRepository;
    private final GeminiVisionService geminiVisionService;
    private final ObjectMapper objectMapper;
    @Value("${file.upload-dir}")
    private String uploadDir;

    public LicenseValidationService(
            DriverRepository driverRepository,
            GeminiVisionService geminiVisionService) {
        this.driverRepository = driverRepository;
        this.geminiVisionService = geminiVisionService;
        this.objectMapper = new ObjectMapper();
    }

    public Driver uploadAndValidateLicense(
            Long driverId,
            MultipartFile file) throws IOException {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() ->
                        new RuntimeException("Driver not found"));

        // 1 — Save the file
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "_"
                + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath,
                StandardCopyOption.REPLACE_EXISTING);

        driver.setLicenseImagePath(filename);

        // 2 — Send to Gemini Vision API
        String mediaType = file.getContentType();
        String aiResponse = geminiVisionService
                .analyzeLicense(file.getBytes(), mediaType);

        driver.setAiExtractedData(aiResponse);

        // 3 — Parse and strictly validate
        try {
            Map<String, Object> extracted = objectMapper
                    .readValue(aiResponse, Map.class);

            boolean aiSaysValid = Boolean.TRUE.equals(
                    extracted.get("isValid"));
            String aiNote = (String) extracted
                    .getOrDefault("validationNote", "");
            String aiLicenseType = (String) extracted
                    .getOrDefault("licenseType", "");
            String aiLicenseNumber = (String) extracted
                    .getOrDefault("licenseNumber", "");
            String aiFirstName = (String) extracted
                    .getOrDefault("firstName", "");
            String aiLastName = (String) extracted
                    .getOrDefault("lastName", "");
            String aiExpiryDate = (String) extracted
                    .getOrDefault("expiryDate", "");

            // Check expiry date
            boolean isNotExpired = checkNotExpired(aiExpiryDate);

            // Check all fields strictly
            boolean nameMatches = matchesName(
                    aiFirstName, driver.getFirstName())
                    && matchesName(aiLastName, driver.getLastName());
            boolean licenseTypeMatches = matchesLicenseType(
                    aiLicenseType, driver.getLicenseType().name());
            boolean licenseNumberMatches = matchesLicenseNumber(
                    aiLicenseNumber, driver.getLicenseNumber());

            driver.setAiValidationNote(
                    "AI extracted — " +
                            "Name: " + aiFirstName + " " + aiLastName + " | " +
                            "License: " + aiLicenseNumber + " | " +
                            "Type: " + aiLicenseType + " | " +
                            "Expiry: " + aiExpiryDate + " | " +
                            "Note: " + aiNote
            );

            // All conditions must pass
            if (aiSaysValid
                    && isNotExpired
                    && nameMatches
                    && licenseTypeMatches
                    && licenseNumberMatches) {

                driver.setValidationStatus(
                        LicenseValidationStatus.APPROVED);
                driver.setAiValidationNote(
                        "Auto-approved by AI. All fields verified. "
                                + driver.getAiValidationNote());

            } else {
                // Build rejection reason
                StringBuilder reason = new StringBuilder(
                        "Validation failed: ");
                if (!isNotExpired)
                    reason.append("License is EXPIRED. ");
                if (!nameMatches)
                    reason.append("Name mismatch (AI read: ")
                            .append(aiFirstName).append(" ")
                            .append(aiLastName).append("). ");
                if (!licenseTypeMatches)
                    reason.append("License type mismatch (AI read: ")
                            .append(aiLicenseType).append("). ");
                if (!licenseNumberMatches)
                    reason.append("License number mismatch (AI read: ")
                            .append(aiLicenseNumber).append("). ");
                if (!aiSaysValid)
                    reason.append("AI could not read license clearly. ");

                driver.setValidationStatus(
                        LicenseValidationStatus.PENDING);
                driver.setAiValidationNote(reason.toString());
            }

        } catch (Exception e) {
            driver.setValidationStatus(
                    LicenseValidationStatus.PENDING);
            driver.setAiValidationNote(
                    "AI could not parse response. Manual review needed.");
        }

        return driverRepository.save(driver);
    }

    // Strict name match — at least 80% similar
    private boolean matchesName(String aiName, String driverName) {
        if (aiName == null || aiName.isBlank()) return false;
        String ai = aiName.trim().toLowerCase()
                .replaceAll("[^a-z]", "");
        String driver = driverName.trim().toLowerCase()
                .replaceAll("[^a-z]", "");
        if (ai.equals(driver)) return true;
        // Check if one contains the other (handles partial reads)
        if (ai.contains(driver) || driver.contains(ai)) return true;
        // Calculate similarity
        return similarity(ai, driver) >= 0.80;
    }

    private boolean matchesLicenseType(String aiType,
                                       String driverType) {
        if (aiType == null || aiType.isBlank()) return false;
        return aiType.trim().equalsIgnoreCase(driverType.trim());
    }

    private boolean matchesLicenseNumber(String aiNumber,
                                         String driverNumber) {
        if (aiNumber == null || aiNumber.isBlank()) return false;
        String ai = aiNumber.replaceAll("[\\s\\-]", "")
                .toLowerCase();
        String driver = driverNumber.replaceAll("[\\s\\-]", "")
                .toLowerCase();
        return ai.equals(driver);
    }

    // Check license is not expired
    private boolean checkNotExpired(String expiryDate) {
        if (expiryDate == null || expiryDate.isBlank()) return false;
        try {
            // Try multiple date formats
            LocalDate expiry = null;
            String[] formats = {
                    "yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy",
                    "dd-MM-yyyy", "yyyy/MM/dd"
            };
            for (String format : formats) {
                try {
                    expiry = LocalDate.parse(expiryDate.trim(),
                            java.time.format.DateTimeFormatter
                                    .ofPattern(format));
                    break;
                } catch (Exception ignored) {}
            }
            if (expiry == null) return false;
            return expiry.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    // Levenshtein similarity score between 0 and 1
    private double similarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;
        int distance = levenshtein(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }

    private int levenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    dp[i][j] = dp[i-1][j-1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i-1][j-1],
                            Math.min(dp[i-1][j], dp[i][j-1]));
                }
            }
        }
        return dp[s1.length()][s2.length()];
        }}
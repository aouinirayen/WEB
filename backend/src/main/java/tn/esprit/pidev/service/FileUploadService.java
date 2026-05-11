package tn.esprit.pidev.service;

import tn.esprit.pidev.exception.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

/**
 * Service for handling file uploads and storage
 * Stores files locally and generates unique names + SHA-256 hashes
 */
@Service
public class FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

    @Value("${app.file.upload.dir:uploads/documents}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}") // 10MB default
    private long maxFileSize;

    private static final String[] ALLOWED_MIME_TYPES = {
        "application/pdf",
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/webp",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    /**
     * Upload file and return the storage path
     * @param file MultipartFile to upload
     * @param userId User ID (for organizing files)
     * @return Relative file path where stored (using forward slashes for cross-platform compatibility)
     */
    public String uploadFile(MultipartFile file, Long userId) {
        try {
            // Validate file
            validateFile(file);

            // Create upload directory if not exists
            String fullUploadDir = uploadDir + File.separator + userId;
            Files.createDirectories(Paths.get(fullUploadDir));

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(fileExtension);
            String filePath = fullUploadDir + File.separator + uniqueFilename;

            // Save file
            Files.write(Paths.get(filePath), file.getBytes());
            logger.info("✓ File uploaded successfully: {}", filePath);

            // Return relative path for storage in database (using forward slashes for cross-platform compatibility)
            return userId + "/" + uniqueFilename;

        } catch (IOException e) {
            logger.error("Failed to upload file: {}", e.getMessage());
            throw new FileUploadException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate SHA-256 hash of file for integrity verification
     * @param file MultipartFile to hash
     * @return SHA-256 hash as hex string
     */
    public String calculateFileHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | IOException e) {
            logger.error("Failed to calculate file hash: {}", e.getMessage());
            throw new FileUploadException("Failed to calculate file hash", e);
        }
    }

    /**
     * Download file and return bytes
     * @param relativePath Relative path from database (may contain forward slashes)
     * @return File bytes
     */
    public byte[] downloadFile(String relativePath) {
        try {
            // Convert forward slashes to platform-specific separator for file system access
            String normalizedPath = relativePath.replace("/", File.separator);
            String fullPath = uploadDir + File.separator + normalizedPath;
            Path filePath = Paths.get(fullPath);

            if (!Files.exists(filePath)) {
                throw new FileUploadException("File not found: " + relativePath);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            logger.error("Failed to download file: {}", e.getMessage());
            throw new FileUploadException("Failed to download file", e);
        }
    }

    /**
     * Delete file from storage
     * @param relativePath Relative path from database (may contain forward slashes)
     */
    public void deleteFile(String relativePath) {
        try {
            // Convert forward slashes to platform-specific separator for file system access
            String normalizedPath = relativePath.replace("/", File.separator);
            String fullPath = uploadDir + File.separator + normalizedPath;
            Path filePath = Paths.get(fullPath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("✓ File deleted: {}", filePath);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete file: {}", e.getMessage());
        }
    }

    /**
     * Verify file hash for integrity
     * @param file File to verify
     * @param expectedHash Expected SHA-256 hash
     * @return true if hash matches
     */
    public boolean verifyFileHash(MultipartFile file, String expectedHash) {
        try {
            String calculatedHash = calculateFileHash(file);
            boolean matches = calculatedHash.equalsIgnoreCase(expectedHash);
            if (!matches) {
                logger.warn("⚠️ File hash mismatch. Expected: {}, Got: {}", expectedHash, calculatedHash);
            }
            return matches;
        } catch (Exception e) {
            logger.error("Failed to verify file hash: {}", e.getMessage());
            return false;
        }
    }

    // Private helper methods

    /**
     * Validate file before upload
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileUploadException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !isAllowedMimeType(mimeType)) {
            throw new FileUploadException("File type not allowed. Allowed types: PDF, Images (JPEG, PNG, GIF, WebP), Office documents");
        }
    }

    /**
     * Check if MIME type is allowed
     */
    private boolean isAllowedMimeType(String mimeType) {
        for (String allowed : ALLOWED_MIME_TYPES) {
            if (allowed.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Generate unique filename with timestamp and UUID
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }

    /**
     * Convert byte array to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}


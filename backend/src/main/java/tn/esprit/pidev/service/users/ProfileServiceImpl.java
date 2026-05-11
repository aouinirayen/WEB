package tn.esprit.pidev.service.users;

import tn.esprit.pidev.dto.users.ProfileResponse;
import tn.esprit.pidev.dto.users.ProfileUpdateRequest;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.exception.UserNotFoundException;
import tn.esprit.pidev.exception.InvalidFileException;
import tn.esprit.pidev.repository.UserRepository;
import tn.esprit.pidev.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * Implementation of ProfileService
 * Handles user profile operations for non-admin users
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_MIME_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @org.springframework.beans.factory.annotation.Value("${app.file.upload.dir:C:/xampp/htdocs/pidev-uploads}")
    private String uploadDir;

    /**
     * Get the current authenticated user's profile
     */
    @Override
    public ProfileResponse getCurrentUserProfile() {
        logger.info("Fetching current user's profile");
        User user = getCurrentUser();
        return convertToProfileResponse(user);
    }

    /**
     * Update the current authenticated user's profile
     * Allows updating: email, name, cin
     * Cannot update username - would invalidate JWT token
     */
    @Override
    @Transactional
    public ProfileResponse updateCurrentUserProfile(ProfileUpdateRequest request) {
        logger.info("Updating current user's profile");

        if (request == null) {
            throw new InvalidFileException("Profile update request cannot be null");
        }

        User user = getCurrentUser();

        // Store the original username to ensure it's never lost during update
        String originalUsername = user.getUsername();

        if (originalUsername == null || originalUsername.isEmpty()) {
            throw new InvalidFileException("Critical error: user has no valid username");
        }

        // Check if email is being changed and is already taken by another user
        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            newEmail = newEmail.trim();
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    throw new InvalidFileException("Email already exists: " + newEmail);
                }
                user.setEmail(newEmail);
            }
        } else if (request.getEmail() != null) {
            // If email field was explicitly provided but is empty, throw error
            throw new InvalidFileException("Email cannot be empty");
        }

        // Update name
        String newName = request.getName();
        if (newName != null) {
            newName = newName.trim();
            if (!newName.isEmpty()) {
                user.setName(newName);
            } else {
                throw new InvalidFileException("Name cannot be empty");
            }
        }

        // Update CIN
        if (request.getCin() != null) {
            user.setCin(request.getCin());
        }

        // CRITICAL: Ensure username is never modified - this could invalidate JWT tokens
        // Re-set it explicitly to prevent any accidental modification
        user.setUsername(originalUsername);

        User updatedUser = userRepository.save(user);
        logger.info("Profile updated successfully for user: " + user.getId());
        return convertToProfileResponse(updatedUser);
    }

    /**
     * Upload or update the current user's profile photo
     */
    @Override
    @Transactional
    public ProfileResponse uploadProfilePhoto(MultipartFile file) {
        logger.info("Uploading profile photo for current user");
        User user = getCurrentUser();

        // Store the original username to ensure it's never lost during update
        String originalUsername = user.getUsername();

        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds maximum limit of 5MB");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (!isAllowedMimeType(contentType)) {
            throw new InvalidFileException("Only image files (JPEG, PNG, GIF, WebP) are allowed");
        }

        try {
            // Delete old photo file if exists
            if (user.getPhoto() != null) {
                try {
                    String oldPhotoPath = new String(user.getPhoto());
                    fileUploadService.deleteFile(oldPhotoPath);
                    logger.info("Old photo file deleted: {}", oldPhotoPath);
                } catch (Exception e) {
                    logger.warn("Could not delete old photo file: {}", e.getMessage());
                }
            }

            // Upload new photo using FileUploadService (saves to htdocs)
            String relativePhotoPath = fileUploadService.uploadFile(file, user.getId());
            
            // Save path and content type to database (not the file bytes)
            user.setPhoto(relativePhotoPath.getBytes());
            user.setPhotoContentType(contentType);

            // CRITICAL: Ensure username is never modified - this could invalidate JWT tokens
            user.setUsername(originalUsername);

            User updatedUser = userRepository.save(user);
            logger.info("Profile photo uploaded successfully for user: {} at path: {} (htdocs)", user.getId(), relativePhotoPath);
            return convertToProfileResponse(updatedUser);
        } catch (Exception e) {
            logger.error("Error uploading photo: {}", e.getMessage());
            throw new InvalidFileException("Error processing file: " + e.getMessage());
        }
    }

    /**
     * Get the current user's profile photo
     */
    @Override
    public byte[] getProfilePhoto() {
        logger.info("Fetching profile photo for current user");
        User user = getCurrentUser();

        if (user.getPhoto() == null) {
            throw new UserNotFoundException("User has no profile photo");
        }

        try {
            // Get path from database and read file
            String relativePath = new String(user.getPhoto());
            return fileUploadService.downloadFile(relativePath);
        } catch (Exception e) {
            logger.error("Error downloading profile photo: {}", e.getMessage());
            throw new InvalidFileException("Error downloading profile photo: " + e.getMessage());
        }
    }

    /**
     * Delete the current user's profile photo
     */
    @Override
    @Transactional
    public ProfileResponse deleteProfilePhoto() {
        logger.info("Deleting profile photo for current user");
        User user = getCurrentUser();

        // Store the original username to ensure it's never lost during update
        String originalUsername = user.getUsername();

        if (user.getPhoto() != null) {
            try {
                // Delete file from file system
                String relativePath = new String(user.getPhoto());
                fileUploadService.deleteFile(relativePath);
            } catch (Exception e) {
                logger.warn("Could not delete profile photo file: {}", e.getMessage());
            }
        }

        user.setPhoto(null);
        user.setPhotoContentType(null);

        // CRITICAL: Ensure username is never modified - this could invalidate JWT tokens
        user.setUsername(originalUsername);

        User updatedUser = userRepository.save(user);
        logger.info("Profile photo deleted successfully for user: {}", user.getId());
        return convertToProfileResponse(updatedUser);
    }

    /**
     * Helper method to get the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    /**
     * Convert User entity to ProfileResponse DTO
     */
    private ProfileResponse convertToProfileResponse(User user) {
        String photoPath = null;
        if (user.getPhoto() != null && user.getPhoto().length > 0) {
            photoPath = new String(user.getPhoto());
        }
        return new ProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getCin(),
                user.getRole().toString(),
                photoPath,
                user.getPhotoContentType(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Check if MIME type is allowed
     */
    private boolean isAllowedMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        for (String allowed : ALLOWED_MIME_TYPES) {
            if (allowed.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }
}


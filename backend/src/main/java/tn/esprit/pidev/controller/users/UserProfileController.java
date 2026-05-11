package tn.esprit.pidev.controller.users;

import tn.esprit.pidev.dto.users.ProfileResponse;
import tn.esprit.pidev.dto.users.ProfileUpdateRequest;
import tn.esprit.pidev.service.users.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * REST Controller for user profile operations
 * Endpoints available to all authenticated non-admin users (AGENT, OPERATOR, PASSENGER)
 * Base path: /api/profile
 */
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private ProfileService profileService;

    /**
     * GET /api/profile - Get current user's profile information
     * 
     * @return ResponseEntity with ProfileResponse containing user's profile details
     */
    @GetMapping
    public ResponseEntity<ProfileResponse> getUserProfile() {
        logger.info("GET /api/profile - Fetching current user's profile");
        ProfileResponse profile = profileService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/profile - Update current user's profile information
     * Users can update: email, name, cin
     * Users cannot update: username, role, enabled status (admin-only)
     * 
     * @param request ProfileUpdateRequest containing fields to update
     * @return ResponseEntity with updated ProfileResponse
     */
    @PutMapping
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @Valid @RequestBody ProfileUpdateRequest request) {
        logger.info("PUT /api/profile - Updating current user's profile");
        ProfileResponse updatedProfile = profileService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * POST /api/profile/photo - Upload or update user's profile photo
     * Accepts: JPEG, PNG, GIF, WebP
     * Maximum size: 5MB
     * 
     * @param file MultipartFile containing the photo
     * @return ResponseEntity with updated ProfileResponse
     */
    @PostMapping("/photo")
    public ResponseEntity<ProfileResponse> uploadProfilePhoto(
            @RequestParam("file") @NotNull(message = "File is required") MultipartFile file) {
        logger.info("POST /api/profile/photo - Uploading profile photo");
        ProfileResponse updatedProfile = profileService.uploadProfilePhoto(file);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * GET /api/profile/photo - Download user's profile photo
     * 
     * @return ResponseEntity with photo bytes and appropriate content type
     */
    @GetMapping("/photo")
    public ResponseEntity<byte[]> downloadProfilePhoto() {
        logger.info("GET /api/profile/photo - Downloading profile photo");
        byte[] photo = profileService.getProfilePhoto();
        
        // Return photo with appropriate headers
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"profile-photo\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(photo);
    }

    /**
     * DELETE /api/profile/photo - Delete user's profile photo
     * 
     * @return ResponseEntity with updated ProfileResponse
     */
    @DeleteMapping("/photo")
    public ResponseEntity<ProfileResponse> deleteProfilePhoto() {
        logger.info("DELETE /api/profile/photo - Deleting profile photo");
        ProfileResponse updatedProfile = profileService.deleteProfilePhoto();
        return ResponseEntity.ok(updatedProfile);
    }
}



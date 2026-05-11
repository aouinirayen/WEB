package tn.esprit.pidev.service.users;

import tn.esprit.pidev.dto.users.ProfileResponse;
import tn.esprit.pidev.dto.users.ProfileUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for user profile operations
 * Provides methods for non-admin users to view and manage their own profile
 */
public interface ProfileService {
    /**
     * Get the current authenticated user's profile
     * @return ProfileResponse with user's profile information
     */
    ProfileResponse getCurrentUserProfile();

    /**
     * Update the current authenticated user's profile (email, name, cin)
     * Non-admin users can only update these fields
     * @param request ProfileUpdateRequest containing fields to update
     * @return ProfileResponse with updated profile information
     */
    ProfileResponse updateCurrentUserProfile(ProfileUpdateRequest request);

    /**
     * Upload or update the current user's profile photo
     * @param file MultipartFile containing the photo
     * @return ProfileResponse with updated profile information
     */
    ProfileResponse uploadProfilePhoto(MultipartFile file);

    /**
     * Get the current user's profile photo
     * @return byte array containing the photo
     */
    byte[] getProfilePhoto();

    /**
     * Delete the current user's profile photo
     * @return ProfileResponse with updated profile information
     */
    ProfileResponse deleteProfilePhoto();
}


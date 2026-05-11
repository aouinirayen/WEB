package tn.esprit.pidev.service.admin;

import tn.esprit.pidev.dto.users.UserResponse;
import tn.esprit.pidev.dto.admin.UserUpdateRequest;
import tn.esprit.pidev.dto.admin.UserCreateRequest;
import tn.esprit.pidev.dto.admin.UserSearchCriteria;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    // READ Operations
    Page<UserResponse> getAllUsers(UserSearchCriteria criteria);
    UserResponse getUserById(Long id);
    Page<UserResponse> searchUsers(UserSearchCriteria criteria);

    // CREATE Operations
    UserResponse createUser(UserCreateRequest request);

    // UPDATE Operations
    UserResponse updateUser(Long id, UserUpdateRequest request);
    UserResponse changeUserRole(Long id, RoleEnum role);
    UserResponse updateUserStatus(Long id, boolean enabled);
    UserResponse banUser(Long id, Integer durationDays);
    UserResponse unbanUser(Long id);

    // DELETE Operations
    void deleteUser(Long id);  // Soft delete

    // PHOTO Operations
    UserResponse uploadUserPhoto(Long userId, MultipartFile file);
    byte[] getUserPhoto(Long userId);
    UserResponse deleteUserPhoto(Long userId);

    // AUTHENTICATION
    User getCurrentUser();
}

package tn.esprit.pidev.service.admin;

import tn.esprit.pidev.dto.users.UserResponse;
import tn.esprit.pidev.dto.admin.UserUpdateRequest;
import tn.esprit.pidev.dto.admin.UserCreateRequest;
import tn.esprit.pidev.dto.admin.UserSearchCriteria;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.exception.UserNotFoundException;
import tn.esprit.pidev.exception.InvalidFileException;
import tn.esprit.pidev.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_MIME_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<UserResponse> getAllUsers(UserSearchCriteria criteria) {
        logger.info("Fetching all users with criteria");
        
        Sort sort = Sort.by(criteria.getSortDir().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, criteria.getSortBy());
        Pageable pageable = PageRequest.of(criteria.getPage(), criteria.getSize(), sort);

        List<User> users = userRepository.searchUsers(criteria.getKeyword(), criteria.getRole());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), users.size());
        List<User> paginatedUsers = users.subList(start, end);

        List<UserResponse> responses = paginatedUsers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, users.size());
    }

    @Override
    public UserResponse getUserById(Long id) {
        logger.info("Fetching user by id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return convertToResponse(user);
    }

    @Override
    public Page<UserResponse> searchUsers(UserSearchCriteria criteria) {
        logger.info("Searching users");
        return getAllUsers(criteria);
    }

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        logger.info("Creating new user: " + request.getUsername());
        
        // DEBUG: Log all received fields
        logger.info("=== USER CREATE REQUEST DEBUG ===");
        logger.info("Username: " + request.getUsername());
        logger.info("Email: " + request.getEmail());
        logger.info("Name: " + request.getName());
        logger.info("Role: " + request.getRole());
        logger.info("Password: " + (request.getPassword() != null ? "***" : "null"));
        logger.info("TransportType: " + request.getTransportType());
        logger.info("=================================");

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidFileException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidFileException("Email already exists: " + request.getEmail());
        }

        // Validate that name is not null or empty
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidFileException("Name is required and cannot be empty");
        }

        // Validate that OPERATOR must have transportType, others must have null
        if (request.getRole() == RoleEnum.OPERATOR) {
            if (request.getTransportType() == null) {
                throw new InvalidFileException("TransportType is required for OPERATOR role");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        user.setEnabled(true);

        // Set transport type ONLY for OPERATOR, null for all others
        if (request.getRole() == RoleEnum.OPERATOR) {
            user.setTransportType(request.getTransportType());
        } else {
            // Force null for non-OPERATOR roles
            user.setTransportType(null);
        }

        User savedUser = userRepository.save(user);
        logger.info("User created successfully: " + savedUser.getId());
        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        logger.info("Updating user with id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Check for uniqueness
        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidFileException("Username already exists");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidFileException("Email already exists");
        }

        // Validate that name is not null or empty
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidFileException("Name is required and cannot be empty");
        }

        // Validate that OPERATOR must have transportType, others must have null
        if (request.getRole() == RoleEnum.OPERATOR) {
            if (request.getTransportType() == null) {
                throw new InvalidFileException("TransportType is required for OPERATOR role");
            }
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole());
        
        // Update cin if provided
        if (request.getCin() != null) {
            user.setCin(request.getCin());
        }
        
        // Update enabled if provided
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        // Set transport type ONLY for OPERATOR, null for all others
        if (request.getRole() == RoleEnum.OPERATOR) {
            user.setTransportType(request.getTransportType());
        } else {
            // Force null for non-OPERATOR roles
            user.setTransportType(null);
        }

        User savedUser = userRepository.save(user);
        logger.info("User updated successfully: " + id);
        return convertToResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Hard deleting user with id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
        logger.info("User hard deleted: " + id);
    }

    @Override
    public UserResponse changeUserRole(Long id, RoleEnum role) {
        logger.info("Changing role for user id: " + id + " to " + role);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setRole(role);
        User savedUser = userRepository.save(user);
        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse updateUserStatus(Long id, boolean enabled) {
        logger.info("Updating status for user id: " + id + " to " + enabled);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);
        logger.info("User status updated successfully: " + id);
        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse banUser(Long id, Integer durationDays) {
        logger.info("Banning user id: " + id + " for " + (durationDays == null ? "permanent" : durationDays + " days"));
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        
        user.setEnabled(false);
        
        if (durationDays == null) {
            // Permanent ban
            user.setInactivatedUntil(null);
        } else {
            // Temporary ban
            LocalDateTime banUntil = LocalDateTime.now().plusDays(durationDays);
            user.setInactivatedUntil(banUntil);
        }
        
        User savedUser = userRepository.save(user);
        logger.info("User banned successfully: " + id);
        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse unbanUser(Long id) {
        logger.info("Unbanning user id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        
        user.setEnabled(true);
        user.setInactivatedUntil(null);
        
        User savedUser = userRepository.save(user);
        logger.info("User unbanned successfully: " + id);
        return convertToResponse(savedUser);
    }

    @Override
    public UserResponse uploadUserPhoto(Long userId, MultipartFile file) {
        logger.info("Uploading photo for user id: " + userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

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
            user.setPhoto(file.getBytes());
            user.setPhotoContentType(contentType);
            User savedUser = userRepository.save(user);
            logger.info("Photo uploaded successfully for user: " + userId);
            return convertToResponse(savedUser);
        } catch (IOException e) {
            logger.severe("Failed to upload photo: " + e.getMessage());
            throw new InvalidFileException("Failed to upload photo", e);
        }
    }

    @Override
    public byte[] getUserPhoto(Long userId) {
        logger.info("Fetching photo for user id: " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (user.getPhoto() == null) {
            throw new UserNotFoundException("User has no photo");
        }

        return user.getPhoto();
    }

    @Override
    public UserResponse deleteUserPhoto(Long userId) {
        logger.info("Deleting photo for user id: " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        user.setPhoto(null);
        user.setPhotoContentType(null);
        User savedUser = userRepository.save(user);
        logger.info("Photo deleted successfully for user: " + userId);
        return convertToResponse(savedUser);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));
    }

    private UserResponse convertToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getName(),
                user.getCin(),
                user.getRole().toString(),
                user.getPhotoContentType(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isEnabled(),
                user.getInactivatedUntil()
        );
    }

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

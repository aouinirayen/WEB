package tn.esprit.pidev.service.auth;

import tn.esprit.pidev.dto.auth.LoginRequest;
import tn.esprit.pidev.dto.auth.RegisterRequest;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.UserRepository;
import tn.esprit.pidev.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private EmailService emailService;

    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setRole(request.getRole() != null ? request.getRole() : RoleEnum.PASSENGER);
        user.setCin(request.getCIN());

        return userRepository.save(user);
    }

    public Authentication authenticateUser(LoginRequest loginRequest) {
        // Use email if provided, otherwise use username
        String identifier = (loginRequest.getEmail() != null && !loginRequest.getEmail().isEmpty()) 
            ? loginRequest.getEmail() 
            : loginRequest.getUsername();
        
        // Check if user exists and validate deactivation status
        User user = userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("User not found: " + identifier));
        
        // Check if user is deactivated
        if (!user.isEnabled()) {
            if (user.getInactivatedUntil() != null && java.time.LocalDateTime.now().isBefore(user.getInactivatedUntil())) {
                // Temporary ban still active
                throw new RuntimeException("Account is deactivated until " + user.getInactivatedUntil());
            } else if (user.getInactivatedUntil() == null) {
                // Permanent ban
                throw new RuntimeException("Account is permanently deactivated");
            } else {
                // Ban has expired, reactivate the user
                logger.info("Ban expired for user: " + identifier + ". Reactivating user.");
                user.setEnabled(true);
                user.setInactivatedUntil(null);
                userRepository.save(user);
            }
        }
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword())
        );
        return authentication;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Generate a password reset token for the user and send email
     * @param email User's email address
     */
    public void forgetPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email '" + email + "' not found"));

        // Generate a unique reset token (UUID)
        String resetToken = UUID.randomUUID().toString();

        // Set token expiry to 24 hours from now
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(24);

        // Save token and expiry to user
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(tokenExpiry);
        userRepository.save(user);

        // Send password reset email
        if (emailService != null) {
            emailService.sendPasswordResetEmail(email, user.getUsername(), resetToken);
        } else {
            logger.warn("Email service not available. Reset token for user '{}': {}", user.getUsername(), resetToken);
        }

        logger.info("✓ Password reset email sent to: {}", email);
    }

    /**
     * Validate and reset the password using the reset token
     * @param resetToken The reset token
     * @param newPassword The new password
     * @return The user with updated password
     */
    public User resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findAll().stream()
                .filter(u -> resetToken.equals(u.getPasswordResetToken()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        // Check if token has expired
        if (user.getPasswordResetTokenExpiry() == null || 
            LocalDateTime.now().isAfter(user.getPasswordResetTokenExpiry())) {
            throw new RuntimeException("Reset token has expired. Please request a new one.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear the reset token and expiry
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);

        User updatedUser = userRepository.save(user);

        // Send confirmation email
        if (emailService != null) {
            emailService.sendPasswordChangeConfirmationEmail(updatedUser.getEmail(), updatedUser.getUsername());
        }

        logger.info("✓ Password reset successfully for user: {}", updatedUser.getUsername());
        return updatedUser;
    }
}
package tn.esprit.pidev.controller.auth;

import tn.esprit.pidev.dto.auth.AuthResponse;
import tn.esprit.pidev.dto.auth.ForgetPasswordRequest;
import tn.esprit.pidev.dto.auth.LoginRequest;
import tn.esprit.pidev.dto.auth.RegisterRequest;
import tn.esprit.pidev.dto.auth.ResetPasswordRequest;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.security.JwtUtil;
import tn.esprit.pidev.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("POST /api/auth/register - Registering new user: {}", request.getUsername());
        User user = authService.registerUser(request);
        return ResponseEntity.ok(new AuthResponse(null, user.getId(), user.getUsername(),
                user.getEmail(), user.getName(), user.getRole().toString()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        logger.info("POST /api/auth/login - Login attempt");
        Authentication authentication = authService.authenticateUser(request);
        String token = jwtUtil.generateJwtToken(authentication);

        // Get the actual User entity from the database using email or username
        String identifier = (request.getEmail() != null && !request.getEmail().isEmpty()) 
            ? request.getEmail() 
            : request.getUsername();
        User user = authService.getUserByUsername(identifier);
        
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), user.getName(), user.getRole().toString()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@NotNull(message = "Authorization header is required") 
                                         @RequestHeader("Authorization") String authHeader) {
        logger.info("POST /api/auth/refresh - Refreshing token");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String newToken = jwtUtil.refreshJwtToken(token);

        if (newToken != null) {
            return ResponseEntity.ok(new AuthResponse(newToken, null, null, null, null, null));
        } else {
            return ResponseEntity.badRequest().body("Failed to refresh token");
        }
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@Valid @RequestBody ForgetPasswordRequest request) {
        try {
            logger.info("Forget password request received for email: {}", request.getEmail());
            authService.forgetPassword(request.getEmail());
            return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
                put("message", "Password reset email has been sent to your email address. Please check your inbox and follow the link to reset your password.");
            }});
        } catch (RuntimeException e) {
            logger.error("Forget password error for email: {}", request.getEmail(), e);
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, String>() {{
                put("error", e.getMessage());
            }});
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(new java.util.HashMap<String, String>() {{
                    put("error", "Passwords do not match");
                }});
            }

            User user = authService.resetPassword(request.getResetToken(), request.getNewPassword());
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("message", "Password has been reset successfully");
                put("username", user.getUsername());
                put("email", user.getEmail());
            }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new java.util.HashMap<String, String>() {{
                put("error", e.getMessage());
            }});
        }
    }
}
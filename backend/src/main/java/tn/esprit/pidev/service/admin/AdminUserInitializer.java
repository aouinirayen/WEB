package tn.esprit.pidev.service.admin;

import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


/**
 * Component to initialize a default admin user on application startup.
 * This ensures that if the database is deleted, the admin account is automatically recreated.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Default admin credentials
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@mail.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin";
    private static final String DEFAULT_ADMIN_NAME = "Administrator";

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initializeDefaultAdminUser();
    }

    /**
     * Initializes the default admin user if it doesn't already exist.
     */
    private void initializeDefaultAdminUser() {
        try {
            // Check if an admin user with ADMIN role already exists
            if (userRepository.existsByRole(RoleEnum.ADMIN)) {
                logger.info("An admin user with role 'ADMIN' already exists in the database.");
                return;
            }

            // Create a new admin user
            User adminUser = new User();
            adminUser.setUsername(DEFAULT_ADMIN_USERNAME);
            adminUser.setEmail(DEFAULT_ADMIN_EMAIL);
            adminUser.setName(DEFAULT_ADMIN_NAME);
            adminUser.setRole(RoleEnum.ADMIN);
            adminUser.setEnabled(true);

            // Hash the password using BCryptPasswordEncoder
            String hashedPassword = passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD);
            adminUser.setPassword(hashedPassword);

            // Timestamps are automatically set by the User constructor

            // Save the admin user to the database
            userRepository.save(adminUser);

            logger.info("✓ Default admin user '{}' has been successfully created with email '{}'.", 
                DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_EMAIL);
            logger.warn("⚠ SECURITY WARNING: Please change the default admin password immediately in production!");

        } catch (Exception e) {
            logger.error("Failed to initialize default admin user", e);
            throw new RuntimeException("Failed to initialize default admin user", e);
        }
    }
}



package tn.esprit.pidev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.pidev.entity.RoleEnum;
import tn.esprit.pidev.entity.User;
import tn.esprit.pidev.repository.UserRepository;

/**
 * Ensures a default ADMIN account exists on startup.
 *
 *   username : admin
 *   password : Admin@1234
 *   email    : admin@transit.tn
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // If an ADMIN role already exists, nothing to do
        if (userRepository.existsByRole(RoleEnum.ADMIN)) {
            log.info("ℹ️  ADMIN account already exists, skipping creation.");
            return;
        }

        // If the username "admin" already exists but is NOT an admin, promote it
        var existing = userRepository.findByUsername("admin");
        if (existing.isPresent()) {
            User user = existing.get();
            user.setRole(RoleEnum.ADMIN);
            user.setEnabled(true);
            // Re-encode password so we know the credentials
            user.setPassword(passwordEncoder.encode("Admin@1234"));
            userRepository.save(user);
            log.info("✅ Existing 'admin' user promoted to ADMIN role  →  password reset to: Admin@1234");
            return;
        }

        // Otherwise create a brand new admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Admin@1234"));
        admin.setEmail("admin@transit.tn");
        admin.setName("Administrator");
        admin.setRole(RoleEnum.ADMIN);
        admin.setCin(99999999L);
        admin.setEnabled(true);
        userRepository.save(admin);
        log.info("✅ Default ADMIN account created  →  username: admin  /  password: Admin@1234");
    }
}

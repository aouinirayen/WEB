package tn.esprit.pidev.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration with Role-Based Access Control (RBAC)
 *
 * Role Hierarchy:
 * - ADMIN: Full access to admin endpoints, user management, document approval
 * - AGENT: Can view and manage agent-specific documents
 * - OPERATOR: Can view and manage operator-specific documents
 * - PASSENGER: Can upload and view personal documents
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ============================================
                        // SWAGGER/OPENAPI ENDPOINTS - No authentication required
                        // ============================================
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()

                        // ============================================
                        // PUBLIC ENDPOINTS - No authentication required
                        // ============================================
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/forget-password").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()

                        // Static files served from htdocs (accessible to all)
                        .requestMatchers("/pidev-uploads/**").permitAll()

                        // AI endpoints
                        .requestMatchers("/api/ai/**").permitAll()

                        // Seed test data endpoint (for testing confidence system)
                        .requestMatchers("/api/covoiturages/seed-test-confiance").permitAll()

                        // Confidence endpoints (public for testing)
                        .requestMatchers("/api/covoiturages/confiance/**").permitAll()
                        .requestMatchers("/api/covoiturages/avis/**").permitAll()

                        // User autocomplete for reservation form
                        .requestMatchers("/api/users/search-autocomplete").permitAll()

                        // Seed test data endpoint
                        .requestMatchers("/api/tickets/seed-data/**").permitAll()

                        // ============================================
                        // AUTHENTICATED ENDPOINTS - All authenticated users
                        // ============================================
                        .requestMatchers("/api/auth/refresh").authenticated()

                        // Organizations - Allow any authenticated user
                        .requestMatchers("/api/organizations/**").authenticated()
                        .requestMatchers("GET", "/api/organizations").authenticated()
                        .requestMatchers("POST", "/api/organizations").authenticated()
                        .requestMatchers("PUT", "/api/organizations/**").authenticated()
                        .requestMatchers("DELETE", "/api/organizations/**").authenticated()

                        // Partners - Allow any authenticated user
                        .requestMatchers("/api/partners/**").authenticated()
                        .requestMatchers("GET", "/api/partners").authenticated()
                        .requestMatchers("POST", "/api/partners").authenticated()
                        .requestMatchers("PUT", "/api/partners/**").authenticated()
                        .requestMatchers("DELETE", "/api/partners/**").authenticated()

                        // Contracts - Allow any authenticated user
                        .requestMatchers("/api/contracts/**").authenticated()
                        .requestMatchers("GET", "/api/contracts").authenticated()
                        .requestMatchers("POST", "/api/contracts").authenticated()
                        .requestMatchers("PUT", "/api/contracts/**").authenticated()
                        .requestMatchers("DELETE", "/api/contracts/**").authenticated()

                        .requestMatchers("/api/contracts/reminders/**").authenticated()
                        .requestMatchers("GET", "/api/contracts/reminders/expiring").authenticated()
                        .requestMatchers("POST", "/api/contracts/reminders/trigger").authenticated()

                        // Pricing Plans - Allow any authenticated user
                        .requestMatchers("/api/pricing-plans/**").authenticated()
                        .requestMatchers("GET", "/api/pricing-plans").authenticated()
                        .requestMatchers("POST", "/api/pricing-plans/**").authenticated()
                        .requestMatchers("PUT", "/api/pricing-plans/**").authenticated()
                        .requestMatchers("DELETE", "/api/pricing-plans/**").authenticated()
                        .requestMatchers("GET", "/api/pricing-plans/**").authenticated()

                        // Reductions - Allow authenticated users
                        .requestMatchers("/api/reductions/**").authenticated()
                        .requestMatchers("GET", "/api/reductions").authenticated()
                        .requestMatchers("POST", "/api/reductions/**").authenticated()
                        .requestMatchers("PUT", "/api/reductions/**").authenticated()
                        .requestMatchers("DELETE", "/api/reductions/**").authenticated()
                        .requestMatchers("GET", "/api/reductions/**").authenticated()

                        // Subscriptions - Allow authenticated users
                        .requestMatchers("/api/subscriptions/**").authenticated()
                        .requestMatchers("GET", "/api/subscriptions").authenticated()
                        .requestMatchers("POST", "/api/subscriptions/**").authenticated()
                        .requestMatchers("PUT", "/api/subscriptions/**").authenticated()
                        .requestMatchers("DELETE", "/api/subscriptions/**").authenticated()
                        .requestMatchers("GET", "/api/subscriptions/**").authenticated()

                        // Payment - Allow authenticated users
                        .requestMatchers("/api/payment/**").authenticated()
                        .requestMatchers("GET", "/api/payment/**").authenticated()
                        .requestMatchers("POST", "/api/payment/**").authenticated()

                        // Loyalty Accounts - Allow authenticated users
                        .requestMatchers("/api/loyalty-accounts/**").authenticated()
                        .requestMatchers("GET", "/api/loyalty-accounts").authenticated()
                        .requestMatchers("GET", "/api/loyalty-accounts/**").authenticated()
                        .requestMatchers("POST", "/api/loyalty-accounts/**").authenticated()
                        .requestMatchers("DELETE", "/api/loyalty-accounts/**").authenticated()

                        // Point Transactions - Allow authenticated users
                        .requestMatchers("/api/point-transactions/**").authenticated()
                        .requestMatchers("GET", "/api/point-transactions").authenticated()
                        .requestMatchers("GET", "/api/point-transactions/**").authenticated()

                        // Reports - Allow authenticated users (admins/operators can trigger)
                        .requestMatchers("/api/reports/**").authenticated()
                        .requestMatchers("POST", "/api/reports/trigger-weekly").authenticated()
                        .requestMatchers("POST", "/api/reports/trigger-expiration-alert").authenticated()

                        // ML Services - Allow authenticated users
                        .requestMatchers("/api/ml/**").authenticated()
                        .requestMatchers("GET", "/api/ml/**").authenticated()
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/documents/**").authenticated()
                        .requestMatchers("GET", "/api/document-types").authenticated()
                        .requestMatchers("GET", "/api/document-types/**").authenticated()
                      //  .requestMatchers("/api/zones/**").hasRole("ADMIN")
                        .requestMatchers("/api/zones/**").authenticated()
                        .requestMatchers("/api/reservations/**").authenticated()
                        .requestMatchers("/api/covoiturages/**").authenticated()
                        .requestMatchers("GET", "/api/tickets/**").permitAll()
                        .requestMatchers("/api/gps/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/routes/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/statistics/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("POST", "/api/tickets/**").authenticated()
                        .requestMatchers("PUT", "/api/tickets/**").authenticated()
                        .requestMatchers("DELETE", "/api/tickets/**").authenticated()

                        // ============================================
                        // ADMIN ENDPOINTS - ADMIN role only
                        // ============================================
                        // Admin User Management - Allow any authenticated user to get/update their own profile (MUST come before generic /admin/* matcher)
                        .requestMatchers("GET", "/api/admin/users/me").authenticated()
                        .requestMatchers("PUT", "/api/admin/users/**").authenticated()

                        // Generic admin endpoints - require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Admin Document Management
                        .requestMatchers("/api/admin/documents/**").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/documents/{id}/approve").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/documents/{id}/reject").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/documents/{id}/request-update").hasRole("ADMIN")

                        .requestMatchers("/api/admin/documents/*/approve").hasRole("ADMIN")
                        .requestMatchers("/api/admin/documents/*/reject").hasRole("ADMIN")
                        .requestMatchers("/api/admin/documents/*/request-update").hasRole("ADMIN")

                        // Admin Document Type Management
                        .requestMatchers("/api/admin/document-types/**").hasRole("ADMIN")
                        .requestMatchers("POST", "/api/admin/document-types").hasRole("ADMIN")
                        .requestMatchers("PUT", "/api/admin/document-types/**").hasRole("ADMIN")
                        .requestMatchers("DELETE", "/api/admin/document-types/**").hasRole("ADMIN")

                        // ============================================
                        // ROLE-SPECIFIC ENDPOINTS
                        // ============================================
                        // AGENT endpoints
                        .requestMatchers("/api/agent/**").hasRole("AGENT")

                        // OPERATOR endpoints
                        .requestMatchers("/api/operator/**").hasRole("OPERATOR")

                        // PASSENGER endpoints
                        .requestMatchers("/api/passenger/**").hasRole("PASSENGER")

                        // ============================================
                        // MULTI-ROLE ENDPOINTS - Multiple roles allowed
                        // ============================================
                        // Users (non-admin) can view their own profile
                        .requestMatchers("GET", "/api/profile").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("PUT", "/api/profile").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("POST", "/api/profile/photo").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("DELETE", "/api/profile/photo").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("GET", "/api/profile/photo").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")

                        // Users (non-admin) can manage their documents
                        .requestMatchers("GET", "/api/documents").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("GET", "/api/documents/**").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("POST", "/api/documents").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("DELETE", "/api/documents/**").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("POST", "/api/documents/{id}/reupload").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")
                        .requestMatchers("GET", "/api/documents/{id}/download").hasAnyRole("AGENT", "OPERATOR", "PASSENGER")

                        // ============================================
                        // NOTIFICATIONS & INCIDENTS - Authenticated users
                        // ============================================
                        .requestMatchers("GET", "/notifications/my").authenticated()
                        .requestMatchers("GET", "/notifications").authenticated()
                        .requestMatchers("GET", "/notifications/**").authenticated()
                        .requestMatchers("POST", "/notifications/**").authenticated()
                        .requestMatchers("PUT", "/notifications/**").authenticated()
                        .requestMatchers("DELETE", "/notifications/**").authenticated()
                        .requestMatchers("PATCH", "/notifications/**").authenticated()

                        .requestMatchers("GET", "/incidents").authenticated()
                        .requestMatchers("GET", "/incidents/**").authenticated()
                        .requestMatchers("POST", "/incidents/**").authenticated()
                        .requestMatchers("PUT", "/incidents/**").authenticated()
                        .requestMatchers("DELETE", "/incidents/**").authenticated()


                        // ============================================
                        // FLEET MANAGEMENT - ADMIN/OPERATOR
                        // ============================================
                        .requestMatchers("/api/maintenance/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/part-usages/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/part-usages/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/vehicles/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/drivers/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/spare-parts/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/fuel-logs/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/trips/**").hasAnyRole("ADMIN", "OPERATOR", "PASSENGER")
                        .requestMatchers("/api/lines/**").hasAnyRole("ADMIN", "OPERATOR", "PASSENGER")
                        .requestMatchers("/api/stops/**").hasAnyRole("ADMIN", "OPERATOR", "PASSENGER")
                        .requestMatchers("/api/schedules/**").hasAnyRole("ADMIN", "OPERATOR", "PASSENGER")
                        .requestMatchers("/api/vehicle-positions/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/gps/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/routes/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/statistics/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/loyalty-accounts/**").authenticated()
                        .requestMatchers("/api/subscriptions/**").authenticated()
                        .requestMatchers("/api/reductions/**").authenticated()
                        .requestMatchers("/api/point-transactions/**").authenticated()
                        .requestMatchers("/api/payment/**").authenticated()
                        .requestMatchers("/api/pricing-plans/**").authenticated()
                        .requestMatchers("/api/ml/**").hasAnyRole("ADMIN", "OPERATOR", "PASSENGER")
                        .requestMatchers("/webhooks/**").permitAll()
                        .requestMatchers("/api/point-transactions/**").authenticated()
                        .requestMatchers("/api/pricing-plans/**").authenticated()
                        .requestMatchers("/api/reductions/**").authenticated()
                        .requestMatchers("/api/subscriptions/**").authenticated()
                        .requestMatchers("/api/payment/**").authenticated()
                        .requestMatchers("/ml/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/vehicle-positions/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/gps/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers("/api/routes/**").hasAnyRole("ADMIN", "OPERATOR")

                        // ============================================
                        // DEFAULT - Deny all other requests
                        // ============================================
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}




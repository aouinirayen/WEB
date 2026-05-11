package tn.esprit.pidev.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${app.jwtRefreshThresholdMs:300000}")
    private int jwtRefreshThresholdMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key())
                .compact();

        logger.debug("Generated JWT token for user: {} with expiration: {} ms", userPrincipal.getUsername(), jwtExpirationMs);
        return token;
    }

    public String generateJwtTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key())
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public String getUserNameFromJwtTokenIgnoreExpiry(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            // Return subject even if token is expired
            return e.getClaims().getSubject();
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return true;
        }
    }

    public boolean isTokenNearExpiry(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Date expirationTime = claims.getExpiration();
            Date now = new Date();
            long timeUntilExpiry = expirationTime.getTime() - now.getTime();
            
            return timeUntilExpiry < jwtRefreshThresholdMs;
        } catch (ExpiredJwtException e) {
            // Token is already expired, definitely needs refresh
            return true;
        } catch (Exception e) {
            logger.error("Error checking token expiry: {}", e.getMessage());
            return false;
        }
    }

    public String refreshJwtToken(String token) {
        try {
            if (isTokenExpired(token)) {
                String username = getUserNameFromJwtTokenIgnoreExpiry(token);
                if (username != null) {
                    return generateJwtTokenFromUsername(username);
                }
            } else if (validateJwtToken(token)) {
                String username = getUserNameFromJwtToken(token);
                return generateJwtTokenFromUsername(username);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error refreshing JWT token: {}", e.getMessage());
            return null;
        }
    }


}




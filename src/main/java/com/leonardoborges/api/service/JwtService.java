package com.leonardoborges.api.service;

import com.leonardoborges.api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        String secret = jwtProperties.getSecret();
        validateJwtSecret(secret);
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    private void validateJwtSecret(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured. Must be set via JWT_SECRET environment variable.");
        }
        
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters long for security. Current length: " + secret.length());
        }
        
        if (secret.length() < 64) {
            log.warn("JWT secret is less than 64 characters. For production, use a secret of at least 64 characters.");
        }
        
        if (isWeakSecret(secret)) {
            throw new IllegalStateException("JWT secret appears to be weak or predictable. Use a cryptographically random secret.");
        }
    }
    
    private boolean isWeakSecret(String secret) {
        if (secret.length() < 32) {
            return true;
        }
        
        String lowerSecret = secret.toLowerCase();
        
        if (lowerSecret.matches("^[a-z]+$") || lowerSecret.matches("^[0-9]+$")) {
            return true;
        }
        
        if (lowerSecret.matches("^(password|secret|key|token|jwt).*") || 
            lowerSecret.matches(".*(password|secret|key|token|jwt)$")) {
            return true;
        }
        
        int length = lowerSecret.length();
        for (int patternLength = 1; patternLength <= length / 2; patternLength++) {
            if (length % patternLength == 0) {
                String pattern = lowerSecret.substring(0, patternLength);
                String repeated = pattern.repeat(length / patternLength);
                if (repeated.equals(lowerSecret)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), false);
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), true);
    }

    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        return createToken(extraClaims, userDetails.getUsername(), false);
    }

    private String createToken(Map<String, Object> claims, String subject, boolean isRefreshToken) {
        Date now = new Date();
        long expirationTime = isRefreshToken ? jwtProperties.getRefreshExpiration() : jwtProperties.getExpiration();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
    
    public Boolean validateRefreshToken(String token) {
        try {
            return isRefreshToken(token) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

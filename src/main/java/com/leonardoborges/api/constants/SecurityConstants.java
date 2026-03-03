package com.leonardoborges.api.constants;

/**
 * Security-related constants.
 * Centralizes security configuration values.
 */
public final class SecurityConstants {
    
    private SecurityConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // Password policy
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;
    public static final boolean REQUIRE_UPPERCASE = true;
    public static final boolean REQUIRE_LOWERCASE = true;
    public static final boolean REQUIRE_DIGIT = true;
    public static final boolean REQUIRE_SPECIAL_CHAR = false; // Optional for better UX
    
    // Account lockout
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final long ACCOUNT_LOCKOUT_DURATION_MINUTES = 30;
    
    // Token expiration
    public static final long MIN_TOKEN_EXPIRATION_TIME_MS = 0;
    
    // JWT secret requirements
    public static final int MIN_JWT_SECRET_LENGTH = 32;
    public static final int RECOMMENDED_JWT_SECRET_LENGTH = 64;
    
    // Bearer token prefix
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
}

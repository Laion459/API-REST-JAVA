package com.leonardoborges.api.util;

/**
 * Utility class for sanitizing sensitive data in logs.
 * Prevents exposure of passwords, tokens, secrets, and other sensitive information.
 */
public final class LogSanitizer {
    
    private static final String SENSITIVE_MASK = "***";
    private static final int MAX_VISIBLE_CHARS = 4;
    
    private LogSanitizer() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Sanitizes a password for logging.
     * Never logs the actual password, only its length.
     * 
     * @param password The password to sanitize (can be null)
     * @return Sanitized string showing only length
     */
    public static String sanitizePassword(String password) {
        if (password == null) {
            return "null";
        }
        return String.format("Password[length=%d]", password.length());
    }
    
    /**
     * Sanitizes a token (JWT, refresh token, etc.) for logging.
     * Shows only first few characters and length.
     * 
     * @param token The token to sanitize (can be null)
     * @return Sanitized string with preview
     */
    public static String sanitizeToken(String token) {
        if (token == null) {
            return "null";
        }
        if (token.length() <= MAX_VISIBLE_CHARS) {
            return SENSITIVE_MASK;
        }
        return token.substring(0, MAX_VISIBLE_CHARS) + "..." + SENSITIVE_MASK + "[length=" + token.length() + "]";
    }
    
    /**
     * Sanitizes a secret (JWT secret, API keys, etc.) for logging.
     * Never logs the actual secret, only its length.
     * 
     * @param secret The secret to sanitize (can be null)
     * @return Sanitized string showing only length
     */
    public static String sanitizeSecret(String secret) {
        if (secret == null) {
            return "null";
        }
        return String.format("Secret[length=%d]", secret.length());
    }
    
    /**
     * Sanitizes an email address for logging.
     * Shows only the domain part, masks the local part.
     * 
     * @param email The email to sanitize (can be null)
     * @return Sanitized email
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return "null";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return SENSITIVE_MASK;
        }
        return SENSITIVE_MASK + email.substring(atIndex);
    }
    
    /**
     * Sanitizes any sensitive string by masking most of it.
     * 
     * @param sensitive The sensitive string to sanitize (can be null)
     * @return Sanitized string
     */
    public static String sanitizeSensitive(String sensitive) {
        if (sensitive == null) {
            return "null";
        }
        if (sensitive.length() <= MAX_VISIBLE_CHARS) {
            return SENSITIVE_MASK;
        }
        return sensitive.substring(0, MAX_VISIBLE_CHARS) + "..." + SENSITIVE_MASK;
    }
    
    /**
     * Checks if a string might contain sensitive information.
     * 
     * @param value The string to check
     * @return true if the string might be sensitive
     */
    public static boolean isPotentiallySensitive(String value) {
        if (value == null) {
            return false;
        }
        String lower = value.toLowerCase();
        return lower.contains("password") || 
               lower.contains("token") || 
               lower.contains("secret") || 
               lower.contains("key") ||
               lower.contains("credential") ||
               lower.contains("auth");
    }
}

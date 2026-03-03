package com.leonardoborges.api.validation;

import com.leonardoborges.api.constants.SecurityConstants;
import com.leonardoborges.api.exception.ValidationException;
import org.springframework.stereotype.Component;

/**
 * Validates password strength according to security policy.
 */
@Component
public class PasswordValidator {
    
    /**
     * Validates password strength.
     * 
     * @param password The password to validate
     * @throws ValidationException if password does not meet requirements
     */
    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ValidationException("Password is required");
        }
        
        if (password.length() < SecurityConstants.MIN_PASSWORD_LENGTH) {
            throw new ValidationException(
                String.format("Password must be at least %d characters long", 
                    SecurityConstants.MIN_PASSWORD_LENGTH));
        }
        
        if (password.length() > SecurityConstants.MAX_PASSWORD_LENGTH) {
            throw new ValidationException(
                String.format("Password must not exceed %d characters", 
                    SecurityConstants.MAX_PASSWORD_LENGTH));
        }
        
        if (SecurityConstants.REQUIRE_UPPERCASE && !containsUppercase(password)) {
            throw new ValidationException("Password must contain at least one uppercase letter");
        }
        
        if (SecurityConstants.REQUIRE_LOWERCASE && !containsLowercase(password)) {
            throw new ValidationException("Password must contain at least one lowercase letter");
        }
        
        if (SecurityConstants.REQUIRE_DIGIT && !containsDigit(password)) {
            throw new ValidationException("Password must contain at least one digit");
        }
        
        if (SecurityConstants.REQUIRE_SPECIAL_CHAR && !containsSpecialChar(password)) {
            throw new ValidationException("Password must contain at least one special character");
        }
    }
    
    private boolean containsUppercase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }
    
    private boolean containsLowercase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }
    
    private boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }
    
    private boolean containsSpecialChar(String password) {
        return password.chars().anyMatch(ch -> 
            !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch));
    }
}

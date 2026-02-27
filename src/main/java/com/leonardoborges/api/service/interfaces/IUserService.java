package com.leonardoborges.api.service.interfaces;

import com.leonardoborges.api.dto.AuthRequest;
import com.leonardoborges.api.dto.AuthResponse;
import com.leonardoborges.api.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface IUserService extends UserDetailsService {
    
    User findUserByUsernameOrEmail(String usernameOrEmail);
    
    AuthResponse register(AuthRequest request);
    
    AuthResponse login(String usernameOrEmail, String password);
    
    AuthResponse refreshToken(String refreshTokenString);
}

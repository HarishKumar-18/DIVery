package com.dlvery.service;

import com.dlvery.dto.UserDTO;
import com.dlvery.model.User;
import com.dlvery.security.JwtUtil;
import com.dlvery.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // ADDED: For logging
import org.slf4j.LoggerFactory; // ADDED: For logging

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // ADDED: Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse login(UserDTO userDTO) {
        try {
            // ADDED: Log request
            logger.debug("Login attempt for username: {}", userDTO.getUsername());
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword())
            );
            User user = userService.findByUsername(userDTO.getUsername())
                    .orElseThrow(() -> new BadRequestException("Invalid credentials"));
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            // ADDED: Log success
            logger.info("Login successful for username: {}", userDTO.getUsername());
            return new AuthResponse(token, user.getRole(), user.getId(), user.getAgentId());
        } catch (Exception e) {
            // ADDED: Log error
            logger.error("Login failed for username: {}. Error: {}", userDTO.getUsername(), e.getMessage());
            throw new BadRequestException("Invalid credentials: " + e.getMessage());
        }
    }

    public String signup(UserDTO userDTO) {
        try {
            // ADDED: Log request
            logger.debug("Signup attempt for username: {}", userDTO.getUsername());
            User user = userService.registerUser(userDTO);
            // ADDED: Log success
            logger.info("Signup successful for username: {}", userDTO.getUsername());
            return "User registered: " + user.getUsername();
        } catch (BadRequestException e) {
            // ADDED: Log error
            logger.error("Signup failed for username: {}. Error: {}", userDTO.getUsername(), e.getMessage());
            throw e;
        }
    }

    // Inner class to match AuthController's response structure
    public static class AuthResponse {
        public String token;
        public String role;
        public String userId;
        public String agentId;

        public AuthResponse(String token, String role, String userId, String agentId) {
            this.token = token;
            this.role = role;
            this.userId = userId;
            this.agentId = agentId;
        }
    }
}
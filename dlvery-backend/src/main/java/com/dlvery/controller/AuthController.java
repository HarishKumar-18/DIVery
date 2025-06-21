package com.dlvery.controller;

import com.dlvery.dto.UserDTO;
import com.dlvery.model.User;
import com.dlvery.security.JwtUtil;
import com.dlvery.service.UserService;
import com.dlvery.service.AuthService; // ADDED: Import AuthService
import com.dlvery.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger; // ADDED: For logging
import org.slf4j.LoggerFactory; // ADDED: For logging

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthService authService; // ADDED: Autowire AuthService

    // ADDED: Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDTO userDTO) {
        try {
            // ADDED: Log request
            logger.debug("Login attempt for username: {}", userDTO.getUsername());
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));
            User user = userService.findByUsername(userDTO.getUsername())
                    .orElseThrow(() -> new BadRequestException("Invalid credentials"));
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            // ADDED: Log success
            logger.info("Login successful for username: {}", userDTO.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, user.getRole(), user.getId(), user.getAgentId()));
        } catch (Exception e) {
            // ADDED: Log error
            logger.error("Login failed for username: {}. Error: {}", userDTO.getUsername(), e.getMessage());
            throw new BadRequestException("Invalid credentials: " + e.getMessage());
        }
    }

    // ADDED: New login endpoint using AuthService
    @PostMapping("/login-service")
    public ResponseEntity<?> loginWithService(@Valid @RequestBody UserDTO userDTO) {
        logger.debug("Login-service attempt for username: {}", userDTO.getUsername());
        return ResponseEntity.ok(authService.login(userDTO));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserDTO userDTO) {
        try {
            // ADDED: Log request
            logger.debug("Signup attempt for username: {}", userDTO.getUsername());
            User user = userService.registerUser(userDTO);
            // ADDED: Log success
            logger.info("Signup successful for username: {}", userDTO.getUsername());
            return ResponseEntity.ok("User registered: " + user.getUsername());
        } catch (BadRequestException e) {
            // ADDED: Log error
            logger.error("Signup failed for username: {}. Error: {}", userDTO.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ADDED: New signup endpoint using AuthService
    @PostMapping("/signup-service")
    public ResponseEntity<?> signupWithService(@Valid @RequestBody UserDTO userDTO) {
        logger.debug("Signup-service attempt for username: {}", userDTO.getUsername());
        return ResponseEntity.ok(authService.signup(userDTO));
    }

    private static class AuthResponse {
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
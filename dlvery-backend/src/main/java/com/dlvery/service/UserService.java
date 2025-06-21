package com.dlvery.service;

import com.dlvery.dto.UserDTO;
import com.dlvery.model.User;
import com.dlvery.repository.UserRepository;
import com.dlvery.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // ADDED: For logging
import org.slf4j.LoggerFactory; // ADDED: For logging

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ADDED: Logger
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public Optional<User> findByUsername(String username) {
        // ADDED: Log query
        logger.debug("Finding user by username: {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        logger.debug("User found: {}", user.isPresent() ? "present" : "not found");
        return user;
    }

    public User updateProfile(String username, User updatedUser) {
        // ADDED: Log request
        logger.debug("Updating profile for username: {}", username);
        User existingUser = findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        User savedUser = userRepository.save(existingUser);
        logger.info("Profile updated for username: {}", username);
        return savedUser;
    }

    public User registerUser(UserDTO userDTO) {
        // ADDED: Log request
        logger.debug("Registering user: {}", userDTO.getUsername());
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        if (!isValidRole(userDTO.getRole())) {
            throw new BadRequestException("Invalid role");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setRole(userDTO.getRole());
        if ("DELIVERY".equals(userDTO.getRole())) {
            user.setAgentId(UUID.randomUUID().toString());
        }

        User savedUser = userRepository.save(user);
        logger.info("User registered: {}", userDTO.getUsername());
        return savedUser;
    }

    public User save(User user) {
        // ADDED: Log request
        logger.debug("Saving user: {}", user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        User savedUser = userRepository.save(user);
        logger.info("User saved: {}", user.getUsername());
        return savedUser;
    }

    private boolean isValidRole(String role) {
        return role != null && (
                role.equals("INVENTORY") ||
                        role.equals("DELIVERY") ||
                        role.equals("ADMIN")
        );
    }
}
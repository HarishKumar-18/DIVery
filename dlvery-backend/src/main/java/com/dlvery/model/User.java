package com.dlvery.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore; // ADDED: To prevent password serialization
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // ADDED: To handle MongoDB proxies

@Document(collection = "users")
@JsonIgnoreProperties(ignoreUnknown = true) // ADDED: Ignore unknown fields during deserialization
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private String role;
    private String agentId;

    // Default constructor
    public User() {
    }

    // Parameterized constructor
    public User(String username, String password, String email, String role, String agentId) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.agentId = agentId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore // ADDED: Prevent password from being serialized in JSON responses
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", agentId='" + agentId + '\'' + // ADDED: Fixed typo in string concatenation
                '}';
    }
}
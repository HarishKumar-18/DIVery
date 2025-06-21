package com.dlvery.repository;

import com.dlvery.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByAgentId(String agentId);
    Optional<User> findByEmail(String email);
}
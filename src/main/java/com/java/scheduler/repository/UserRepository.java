package com.java.scheduler.repository;

import com.java.scheduler.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    // Critical for Login and Password Reset
    Optional<User> findByEmail(String email);
    
    // Check if email is already taken during Registration
    boolean existsByEmail(String email);
}
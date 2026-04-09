package com.java.scheduler.service;

import com.java.scheduler.domain.User;
import com.java.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. SECURE REGISTRATION
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }
        
        // Hash the password before saving to the 'equipmentscheduler' DB
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        
        return userRepository.save(user);
    }

    // 2. SECURE LOGIN
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // BCrypt handles the salt comparison automatically
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return user; 
        } else {
            throw new RuntimeException("Invalid email or password");
        }
    }

    // 3. SECURE PASSWORD RESET
    @Transactional
    public void resetPassword(String email, String newRawPassword) {
        // 1. Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with this email does not exist."));
        
        // 2. Encode the new password using your existing PasswordEncoder (e.g., BCrypt)
        String encodedPassword = passwordEncoder.encode(newRawPassword);
        
        // 3. Update and save
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }
}
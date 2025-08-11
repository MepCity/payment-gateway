package com.payment.gateway.service;

import com.payment.gateway.model.User;
import com.payment.gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public User createUser(String username, String email, String password, String firstName, String lastName, User.UserRole role, String merchantId) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }
        
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setMerchantId(merchantId);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}, role: {}", username, role);
        
        return savedUser;
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Transactional
    public void createDefaultUsers() {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            createUser("admin", "admin@paymentgateway.com", "admin123", "System", "Admin", User.UserRole.ADMIN, null);
            log.info("Default admin user created");
        }
        
        // Create merchant admin user if not exists
        if (!userRepository.existsByUsername("merchant001")) {
            createUser("merchant001", "merchant001@test.com", "merchant123", "Merchant", "User", User.UserRole.MERCHANT_ADMIN, "MERCH001");
            log.info("Default merchant user created");
        }
    }
}

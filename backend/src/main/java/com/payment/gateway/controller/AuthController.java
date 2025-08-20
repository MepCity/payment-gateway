package com.payment.gateway.controller;

import com.payment.gateway.dto.LoginRequest;
import com.payment.gateway.dto.LoginResponse;
import com.payment.gateway.service.MerchantAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final MerchantAuthService merchantAuthService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for merchant: {}", request.getEmail());
        
        try {
            LoginResponse response = merchantAuthService.authenticate(request);
            
            if (response.isSuccess()) {
                log.info("Login successful for merchant: {}", request.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Login failed for merchant: {}", request.getEmail());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error during login for merchant {}: {}", request.getEmail(), e.getMessage());
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Authentication failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout request received");
        
        try {
            merchantAuthService.logout(token);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Logout failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<LoginResponse> getProfile(@RequestHeader("Authorization") String token) {
        log.info("Profile request received");
        
        try {
            LoginResponse response = merchantAuthService.getProfile(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting profile: {}", e.getMessage());
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to get profile: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}

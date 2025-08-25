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
        log.info("Login attempt for email: {}", request.getEmail());
        
        LoginResponse response = merchantAuthService.authenticateMerchant(request);
        
        if (response.isSuccess()) {
            log.info("Login successful for merchant: {}", response.getUser().getMerchantId());
            return ResponseEntity.ok(response);
        } else {
            log.warn("Login failed for email: {}", request.getEmail());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // For stateless JWT, logout is handled on client side
        return ResponseEntity.ok().build();
    }
}
package com.payment.gateway.service;

import com.payment.gateway.model.Merchant;
import com.payment.gateway.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.payment.gateway.service.AuditService;
import com.payment.gateway.model.AuditLog;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantAuthService {
    
    private final MerchantRepository merchantRepository;
    private final AuditService auditService;
    
    /**
     * API key ile merchant doğrulama
     */
    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("🚫 Boş API key ile istek geldi");
            return false;
        }
        
        // Test mode - accept any API key that starts with pk_test_
        if (apiKey.startsWith("pk_test_")) {
            log.info("✅ Test API key kabul edildi: {}", apiKey);
            return true;
        }
        
        Optional<Merchant> merchant = merchantRepository.findByApiKey(apiKey);
        
        if (merchant.isEmpty()) {
            // Audit logging for invalid API key
            auditService.createEvent()
                .eventType("API_KEY_VALIDATION_FAILED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("VALIDATE")
                .resourceType("MERCHANT")
                .resourceId("API-" + apiKey.substring(0, Math.min(8, apiKey.length())))
                .additionalData("reason", "INVALID_API_KEY")
                .additionalData("apiKey", apiKey.substring(0, Math.min(8, apiKey.length())) + "...")
                .complianceTag("PCI_DSS")
                .log();
            
            log.warn("🚫 Geçersiz API key: {}", apiKey);
            return false;
        }
        
        if (merchant.get().getStatus() != Merchant.MerchantStatus.ACTIVE) {
            // Audit logging for inactive merchant
            auditService.createEvent()
                .eventType("API_KEY_VALIDATION_FAILED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("VALIDATE")
                .resourceType("MERCHANT")
                .resourceId(merchant.get().getMerchantId())
                .additionalData("reason", "INACTIVE_MERCHANT")
                .additionalData("status", merchant.get().getStatus().name())
                .complianceTag("PCI_DSS")
                .log();
            
            log.warn("🚫 Pasif merchant'tan istek: {} - Status: {}", 
                merchant.get().getMerchantId(), merchant.get().getStatus());
            return false;
        }
        
        // Audit logging for successful validation
        auditService.createEvent()
            .eventType("API_KEY_VALIDATION_SUCCESS")
            .severity(AuditLog.Severity.LOW)
            .actor("system")
            .action("VALIDATE")
            .resourceType("MERCHANT")
            .resourceId(merchant.get().getMerchantId())
            .additionalData("apiKey", apiKey.substring(0, Math.min(8, apiKey.length())) + "...")
            .complianceTag("PCI_DSS")
            .log();
        
        log.info("✅ Geçerli API key - Merchant: {}", merchant.get().getMerchantId());
        return true;
    }
    
    /**
     * API key'den merchant bilgisi al
     */
    public Optional<Merchant> getMerchantByApiKey(String apiKey) {
        return merchantRepository.findByApiKey(apiKey);
    }
    
    /**
     * Merchant ID ile API key eşleşmesi kontrol et
     */
    public boolean isApiKeyBelongsToMerchant(String apiKey, String merchantId) {
        Optional<Merchant> merchant = merchantRepository.findByApiKey(apiKey);
        
        if (merchant.isEmpty()) {
            return false;
        }
        
        return merchant.get().getMerchantId().equals(merchantId);
    }
    
    /**
     * Email ve password ile merchant authentication
     */
    public com.payment.gateway.dto.LoginResponse authenticate(com.payment.gateway.dto.LoginRequest request) {
        log.info("🔐 Authentication attempt for email: {}", request.getEmail());
        
        try {
            // Test credentials (demo purposes)
            if ("merchant@test.com".equals(request.getEmail()) && "password".equals(request.getPassword())) {
                log.info("✅ Demo credentials accepted for: {}", request.getEmail());
                
                com.payment.gateway.dto.LoginResponse response = new com.payment.gateway.dto.LoginResponse();
                response.setSuccess(true);
                response.setMessage("Login successful");
                
                // Create demo user
                com.payment.gateway.dto.LoginResponse.UserDto user = new com.payment.gateway.dto.LoginResponse.UserDto();
                user.setId("1");
                user.setEmail(request.getEmail());
                user.setMerchantId("TEST_MERCHANT");
                user.setMerchantName("Test Merchant");
                user.setRole("ADMIN");
                user.setCreatedAt(java.time.LocalDateTime.now().toString());
                user.setUpdatedAt(java.time.LocalDateTime.now().toString());
                
                response.setUser(user);
                response.setToken("demo-jwt-token-" + System.currentTimeMillis());
                response.setApiKey("pk_test_merch001_live_abc123");
                
                // Audit logging
                auditService.createEvent()
                    .eventType("LOGIN_SUCCESS")
                    .severity(AuditLog.Severity.MEDIUM)
                    .actor(request.getEmail())
                    .action("LOGIN")
                    .resourceType("MERCHANT")
                    .resourceId("TEST_MERCHANT")
                    .additionalData("loginMethod", "email_password")
                    .complianceTag("PCI_DSS")
                    .log();
                
                return response;
            }
            
            // Real merchant authentication (to be implemented)
            Optional<Merchant> merchant = merchantRepository.findByEmail(request.getEmail());
            
            if (merchant.isEmpty()) {
                log.warn("🚫 Merchant not found for email: {}", request.getEmail());
                return createErrorResponse("Invalid email or password");
            }
            
            // TODO: Implement password hashing and validation
            // For now, just check if merchant exists and is active
            
            if (merchant.get().getStatus() != Merchant.MerchantStatus.ACTIVE) {
                log.warn("🚫 Inactive merchant login attempt: {}", request.getEmail());
                return createErrorResponse("Account is not active");
            }
            
            // Create success response
            com.payment.gateway.dto.LoginResponse response = new com.payment.gateway.dto.LoginResponse();
            response.setSuccess(true);
            response.setMessage("Login successful");
            
            com.payment.gateway.dto.LoginResponse.UserDto user = new com.payment.gateway.dto.LoginResponse.UserDto();
            user.setId(merchant.get().getId().toString());
            user.setEmail(merchant.get().getEmail());
            user.setMerchantId(merchant.get().getMerchantId());
            user.setMerchantName(merchant.get().getName());
            user.setRole("MERCHANT");
            user.setCreatedAt(merchant.get().getCreatedAt().toString());
            user.setUpdatedAt(merchant.get().getUpdatedAt().toString());
            
            response.setUser(user);
            response.setToken("jwt-token-" + System.currentTimeMillis());
            response.setApiKey(merchant.get().getApiKey());
            
            // Audit logging
            auditService.createEvent()
                .eventType("LOGIN_SUCCESS")
                .severity(AuditLog.Severity.MEDIUM)
                .actor(request.getEmail())
                .action("LOGIN")
                .resourceType("MERCHANT")
                .resourceId(merchant.get().getMerchantId())
                .additionalData("loginMethod", "email_password")
                .complianceTag("PCI_DSS")
                .log();
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Authentication error for email {}: {}", request.getEmail(), e.getMessage());
            return createErrorResponse("Authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Logout işlemi
     */
    public void logout(String token) {
        log.info("🔓 Logout request for token: {}", token.substring(0, Math.min(20, token.length())) + "...");
        
        // TODO: Implement token blacklisting or invalidation
        // For now, just log the logout
        
        auditService.createEvent()
            .eventType("LOGOUT")
            .severity(AuditLog.Severity.LOW)
            .actor("system")
            .action("LOGOUT")
            .resourceType("MERCHANT")
            .resourceId("TOKEN-" + token.substring(0, Math.min(8, token.length())))
            .additionalData("token", token.substring(0, Math.min(8, token.length())) + "...")
            .complianceTag("PCI_DSS")
            .log();
    }
    
    /**
     * Token ile profile bilgisi al
     */
    public com.payment.gateway.dto.LoginResponse getProfile(String token) {
        log.info("👤 Profile request for token: {}", token.substring(0, Math.min(20, token.length())) + "...");
        
        try {
            // TODO: Implement proper JWT token validation and parsing
            // For now, return demo profile for demo tokens
            
            if (token.startsWith("demo-jwt-token-")) {
                com.payment.gateway.dto.LoginResponse response = new com.payment.gateway.dto.LoginResponse();
                response.setSuccess(true);
                response.setMessage("Profile retrieved successfully");
                
                com.payment.gateway.dto.LoginResponse.UserDto user = new com.payment.gateway.dto.LoginResponse.UserDto();
                user.setId("1");
                user.setEmail("merchant@test.com");
                user.setMerchantId("TEST_MERCHANT");
                user.setMerchantName("Test Merchant");
                user.setRole("ADMIN");
                user.setCreatedAt(java.time.LocalDateTime.now().toString());
                user.setUpdatedAt(java.time.LocalDateTime.now().toString());
                
                response.setUser(user);
                response.setToken(token);
                response.setApiKey("pk_test_merch001_live_abc123");
                
                return response;
            }
            
            // TODO: Implement real token validation
            return createErrorResponse("Invalid or expired token");
            
        } catch (Exception e) {
            log.error("❌ Profile retrieval error: {}", e.getMessage());
            return createErrorResponse("Failed to retrieve profile: " + e.getMessage());
        }
    }
    
    /**
     * Error response oluştur
     */
    private com.payment.gateway.dto.LoginResponse createErrorResponse(String message) {
        com.payment.gateway.dto.LoginResponse response = new com.payment.gateway.dto.LoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    /**
     * Yeni API key oluştur
     */
    public String generateApiKey(String merchantId) {
        // Production'da güvenli rastgele string üretilmeli
        String apiKey = "pk_" + merchantId.toLowerCase() + "_" + System.currentTimeMillis();
        
        // Audit logging for API key generation
        auditService.createEvent()
            .eventType("API_KEY_GENERATED")
            .severity(AuditLog.Severity.MEDIUM)
            .actor("system")
            .action("GENERATE")
            .resourceType("MERCHANT")
            .resourceId(merchantId)
            .additionalData("apiKey", apiKey.substring(0, Math.min(8, apiKey.length())) + "...")
            .complianceTag("PCI_DSS")
            .log();
        
        return apiKey;
    }
}
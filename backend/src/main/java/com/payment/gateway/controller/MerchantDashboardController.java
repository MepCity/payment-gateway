package com.payment.gateway.controller;

import com.payment.gateway.dto.MerchantResponse;
import com.payment.gateway.dto.WebhookRequest;
import com.payment.gateway.dto.WebhookResponse;
import com.payment.gateway.model.Merchant;
import com.payment.gateway.service.MerchantService;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/merchant-dashboard")
@RequiredArgsConstructor
@Slf4j
public class MerchantDashboardController {
    
    private final MerchantService merchantService;
    private final PaymentService paymentService;
    private final WebhookService webhookService;
    
    /**
     * Merchant dashboard ana sayfası
     */
    @GetMapping("/{merchantId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable String merchantId) {
        log.info("📊 Merchant dashboard getiriliyor: {}", merchantId);
        
        // Merchant bilgilerini getir
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Dashboard verilerini hazırla
        Map<String, Object> dashboard = new java.util.HashMap<>();
        dashboard.put("merchant", merchant);
        dashboard.put("status", merchant.getStatus());
        dashboard.put("createdAt", merchant.getCreatedAt());
        
        // Burada payment istatistikleri, son işlemler vb. eklenebilir
        // Örnek olarak basit veriler ekliyorum
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Merchant profil bilgileri
     */
    @GetMapping("/{merchantId}/profile")
    public ResponseEntity<MerchantResponse> getProfile(@PathVariable String merchantId) {
        log.info("👤 Merchant profil bilgileri getiriliyor: {}", merchantId);
        return merchantService.getMerchantByMerchantId(merchantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Merchant webhook ayarları
     */
    @GetMapping("/{merchantId}/webhook-settings")
    public ResponseEntity<Map<String, Object>> getWebhookSettings(@PathVariable String merchantId) {
        log.info("🔗 Merchant webhook ayarları getiriliyor: {}", merchantId);
        
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> webhookSettings = new java.util.HashMap<>();
        webhookSettings.put("webhookUrl", merchant.getWebhookUrl());
        webhookSettings.put("webhookEvents", merchant.getWebhookEvents());
        
        return ResponseEntity.ok(webhookSettings);
    }
    
    /**
     * Merchant API bilgileri (sadece API key prefix'i)
     */
    @GetMapping("/{merchantId}/api-info")
    public ResponseEntity<Map<String, Object>> getApiInfo(@PathVariable String merchantId) {
        log.info("🔑 Merchant API bilgileri getiriliyor: {}", merchantId);
        
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> apiInfo = new java.util.HashMap<>();
        apiInfo.put("merchantId", merchant.getMerchantId());
        apiInfo.put("status", merchant.getStatus());
        apiInfo.put("createdAt", merchant.getCreatedAt());
        
        return ResponseEntity.ok(apiInfo);
    }
    
    /**
     * Merchant aktivite durumu
     */
    @GetMapping("/{merchantId}/activity-status")
    public ResponseEntity<Map<String, Object>> getActivityStatus(@PathVariable String merchantId) {
        log.info("📈 Merchant aktivite durumu getiriliyor: {}", merchantId);
        
        MerchantResponse merchant = merchantService.getMerchantByMerchantId(merchantId)
                .orElse(null);
        
        if (merchant == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> activityStatus = new java.util.HashMap<>();
        activityStatus.put("merchantId", merchant.getMerchantId());
        activityStatus.put("status", merchant.getStatus());
        activityStatus.put("lastUpdated", merchant.getUpdatedAt());
        activityStatus.put("isActive", "ACTIVE".equals(merchant.getStatus()));
        
        return ResponseEntity.ok(activityStatus);
    }
    
    /**
     * Test webhook'larını oluştur
     */
    @PostMapping("/{merchantId}/setup-test-webhooks")
    public ResponseEntity<Map<String, Object>> setupTestWebhooks(@PathVariable String merchantId) {
        log.info("🔧 Setting up test webhooks for merchant: {}", merchantId);
        
        try {
            // Test webhook URL'leri
            String[] eventTypes = {
                "PAYMENT_COMPLETED", "PAYMENT_FAILED", 
                "REFUND_COMPLETED", "REFUND_FAILED"
            };
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("merchantId", merchantId);
            result.put("createdWebhooks", new java.util.ArrayList<>());
            
            for (String eventType : eventTypes) {
                WebhookRequest webhookRequest = new WebhookRequest();
                webhookRequest.setMerchantId(merchantId);
                webhookRequest.setUrl("https://webhook.site/" + java.util.UUID.randomUUID().toString());
                webhookRequest.setEventType(eventType);
                webhookRequest.setSecretKey("test-secret-" + merchantId);
                webhookRequest.setMaxRetries(3);
                webhookRequest.setTimeoutSeconds(30);
                webhookRequest.setDescription("Test webhook for " + eventType);
                webhookRequest.setIsActive(true);
                
                WebhookResponse response = webhookService.createWebhook(webhookRequest);
                
                if (response.isSuccess()) {
                    Map<String, Object> webhookInfo = new java.util.HashMap<>();
                    webhookInfo.put("eventType", eventType);
                    webhookInfo.put("webhookId", response.getWebhookId());
                    webhookInfo.put("url", response.getUrl());
                    ((java.util.List<Object>) result.get("createdWebhooks")).add(webhookInfo);
                }
            }
            
            result.put("success", true);
            result.put("message", "Test webhooks created successfully");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error setting up test webhooks for merchant: {}", merchantId, e);
            
            Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Failed to create test webhooks: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
}

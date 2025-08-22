package com.payment.gateway.controller;

import com.payment.gateway.dto.*;
import com.payment.gateway.model.Merchant;
import com.payment.gateway.service.MerchantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/merchants")
@RequiredArgsConstructor
@Slf4j
public class MerchantController {
    
    private final MerchantService merchantService;
    
    /**
     * Tüm merchant'ları listele
     */
    @GetMapping
    public ResponseEntity<List<MerchantResponse>> getAllMerchants() {
        log.info("📋 Tüm merchant'lar listeleniyor");
        List<MerchantResponse> merchants = merchantService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }
    
    /**
     * ID ile merchant getir
     */
    @GetMapping("/{id}")
    public ResponseEntity<MerchantResponse> getMerchantById(@PathVariable Long id) {
        log.info("🔍 ID ile merchant aranıyor: {}", id);
        return merchantService.getMerchantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Merchant ID ile merchant getir
     */
    @GetMapping("/merchant-id/{merchantId}")
    public ResponseEntity<MerchantResponse> getMerchantByMerchantId(@PathVariable String merchantId) {
        log.info("🔍 Merchant ID ile merchant aranıyor: {}", merchantId);
        return merchantService.getMerchantByMerchantId(merchantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Status'e göre merchant'ları listele
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MerchantResponse>> getMerchantsByStatus(@PathVariable Merchant.MerchantStatus status) {
        log.info("📋 Status'e göre merchant'lar listeleniyor: {}", status);
        List<MerchantResponse> merchants = merchantService.getMerchantsByStatus(status);
        return ResponseEntity.ok(merchants);
    }
    
    /**
     * Aktif merchant'ları listele
     */
    @GetMapping("/active")
    public ResponseEntity<List<MerchantResponse>> getActiveMerchants() {
        log.info("📋 Aktif merchant'lar listeleniyor");
        List<MerchantResponse> merchants = merchantService.getActiveMerchants();
        return ResponseEntity.ok(merchants);
    }
    
    /**
     * Yeni merchant oluştur
     */
    @PostMapping
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody MerchantRequest request) {
        log.info("➕ Yeni merchant oluşturuluyor: {}", request.getMerchantId());
        try {
            MerchantResponse merchant = merchantService.createMerchant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(merchant);
        } catch (RuntimeException e) {
            log.error("❌ Merchant oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Merchant güncelle
     */
    @PutMapping("/{merchantId}")
    public ResponseEntity<MerchantResponse> updateMerchant(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantUpdateRequest request) {
        log.info("✏️ Merchant güncelleniyor: {}", merchantId);
        try {
            return merchantService.updateMerchant(merchantId, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            log.error("❌ Merchant güncelleme hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Merchant status güncelle
     */
    @PatchMapping("/{merchantId}/status")
    public ResponseEntity<MerchantResponse> updateMerchantStatus(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantStatusRequest request) {
        log.info("🔄 Merchant status güncelleniyor: {} -> {}", merchantId, request.getStatus());
        return merchantService.updateMerchantStatus(merchantId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Merchant sil (soft delete)
     */
    @DeleteMapping("/{merchantId}")
    public ResponseEntity<Void> deleteMerchant(@PathVariable String merchantId) {
        log.info("🗑️ Merchant siliniyor: {}", merchantId);
        boolean deleted = merchantService.deleteMerchant(merchantId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * API key yenile
     */
    @PostMapping("/{merchantId}/regenerate-api-key")
    public ResponseEntity<ApiKeyResponse> regenerateApiKey(@PathVariable String merchantId) {
        log.info("🔑 API key yenileniyor: {}", merchantId);
        ApiKeyResponse response = merchantService.regenerateApiKey(merchantId);
        
        if (response.getApiKey() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Merchant istatistikleri
     */
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getMerchantCount() {
        long count = merchantService.getMerchantCount();
        return ResponseEntity.ok(count);
    }
    
    /**
     * Status'e göre merchant sayısı
     */
    @GetMapping("/stats/count/{status}")
    public ResponseEntity<Long> getMerchantCountByStatus(@PathVariable Merchant.MerchantStatus status) {
        long count = merchantService.getMerchantCountByStatus(status);
        return ResponseEntity.ok(count);
    }
}
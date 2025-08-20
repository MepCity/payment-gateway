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
import java.util.Map;

@RestController
@RequestMapping("/v1/admin/merchants")
@RequiredArgsConstructor
@Slf4j
public class AdminMerchantController {
    
    private final MerchantService merchantService;
    
    /**
     * Admin: Tüm merchant'ları listele (pagination ile)
     */
    @GetMapping
    public ResponseEntity<List<MerchantResponse>> getAllMerchants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("👑 Admin: Tüm merchant'lar listeleniyor - Sayfa: {}, Boyut: {}", page, size);
        List<MerchantResponse> merchants = merchantService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }
    
    /**
     * Admin: Merchant detaylarını getir (tüm bilgiler dahil)
     */
    @GetMapping("/{merchantId}/details")
    public ResponseEntity<MerchantResponse> getMerchantDetails(@PathVariable String merchantId) {
        log.info("👑 Admin: Merchant detayları getiriliyor: {}", merchantId);
        return merchantService.getMerchantByMerchantId(merchantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Admin: Merchant oluştur
     */
    @PostMapping
    public ResponseEntity<MerchantResponse> createMerchant(@Valid @RequestBody MerchantRequest request) {
        log.info("👑 Admin: Yeni merchant oluşturuluyor: {}", request.getMerchantId());
        try {
            MerchantResponse merchant = merchantService.createMerchant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(merchant);
        } catch (RuntimeException e) {
            log.error("❌ Admin: Merchant oluşturma hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Admin: Merchant güncelle
     */
    @PutMapping("/{merchantId}")
    public ResponseEntity<MerchantResponse> updateMerchant(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantUpdateRequest request) {
        log.info("👑 Admin: Merchant güncelleniyor: {}", merchantId);
        try {
            return merchantService.updateMerchant(merchantId, request)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            log.error("❌ Admin: Merchant güncelleme hatası: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Admin: Merchant status güncelle
     */
    @PatchMapping("/{merchantId}/status")
    public ResponseEntity<MerchantResponse> updateMerchantStatus(
            @PathVariable String merchantId,
            @Valid @RequestBody MerchantStatusRequest request) {
        log.info("👑 Admin: Merchant status güncelleniyor: {} -> {}", merchantId, request.getStatus());
        return merchantService.updateMerchantStatus(merchantId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Admin: Merchant'ı kalıcı olarak sil
     */
    @DeleteMapping("/{merchantId}/permanent")
    public ResponseEntity<Void> permanentlyDeleteMerchant(@PathVariable String merchantId) {
        log.info("👑 Admin: Merchant kalıcı olarak siliniyor: {}", merchantId);
        boolean deleted = merchantService.deleteMerchant(merchantId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Admin: API key yenile
     */
    @PostMapping("/{merchantId}/regenerate-api-key")
    public ResponseEntity<ApiKeyResponse> regenerateApiKey(@PathVariable String merchantId) {
        log.info("👑 Admin: API key yenileniyor: {}", merchantId);
        ApiKeyResponse response = merchantService.regenerateApiKey(merchantId);
        
        if (response.getApiKey() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Admin: Toplu status güncelleme
     */
    @PatchMapping("/bulk-status")
    public ResponseEntity<Map<String, String>> bulkUpdateStatus(
            @RequestBody Map<String, Merchant.MerchantStatus> updates) {
        log.info("👑 Admin: Toplu status güncelleme: {} merchant", updates.size());
        
        Map<String, String> results = new java.util.HashMap<>();
        
        updates.forEach((merchantId, status) -> {
            try {
                MerchantStatusRequest statusRequest = new MerchantStatusRequest(status, "Toplu güncelleme");
                merchantService.updateMerchantStatus(merchantId, statusRequest);
                results.put(merchantId, "Başarılı");
            } catch (Exception e) {
                results.put(merchantId, "Hata: " + e.getMessage());
            }
        });
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Admin: Merchant istatistikleri
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMerchantStats() {
        log.info("👑 Admin: Merchant istatistikleri getiriliyor");
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalCount", merchantService.getMerchantCount());
        stats.put("activeCount", merchantService.getMerchantCountByStatus(Merchant.MerchantStatus.ACTIVE));
        stats.put("inactiveCount", merchantService.getMerchantCountByStatus(Merchant.MerchantStatus.INACTIVE));
        stats.put("suspendedCount", merchantService.getMerchantCountByStatus(Merchant.MerchantStatus.SUSPENDED));
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Admin: Merchant arama (basit text search)
     */
    @GetMapping("/search")
    public ResponseEntity<List<MerchantResponse>> searchMerchants(
            @RequestParam String query) {
        log.info("👑 Admin: Merchant arama: {}", query);
        
        // Basit arama implementasyonu - production'da daha gelişmiş arama yapılabilir
        List<MerchantResponse> allMerchants = merchantService.getAllMerchants();
        List<MerchantResponse> filteredMerchants = allMerchants.stream()
                .filter(merchant -> 
                    merchant.getMerchantId().toLowerCase().contains(query.toLowerCase()) ||
                    merchant.getName().toLowerCase().contains(query.toLowerCase()) ||
                    merchant.getEmail().toLowerCase().contains(query.toLowerCase()))
                .toList();
        
        return ResponseEntity.ok(filteredMerchants);
    }
}

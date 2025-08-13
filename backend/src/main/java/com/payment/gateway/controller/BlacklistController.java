package com.payment.gateway.controller;

import com.payment.gateway.model.BlacklistEntry;
import com.payment.gateway.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Blacklist Management Controller
 * Fraud detection için blacklist yönetimi
 */
@RestController
@RequestMapping("/v1/blacklist")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class BlacklistController {
    
    private final BlacklistService blacklistService;
    
    /**
     * Blacklist'e entry ekle
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToBlacklist(@RequestBody Map<String, Object> request) {
        try {
            BlacklistEntry.BlacklistType type = BlacklistEntry.BlacklistType.valueOf((String) request.get("type"));
            String value = (String) request.get("value");
            BlacklistEntry.BlacklistReason reason = BlacklistEntry.BlacklistReason.valueOf((String) request.get("reason"));
            String description = (String) request.get("description");
            String addedBy = (String) request.get("addedBy");
            String merchantId = (String) request.get("merchantId");
            
            LocalDateTime expiresAt = null;
            if (request.get("expiresAt") != null) {
                expiresAt = LocalDateTime.parse((String) request.get("expiresAt"));
            }
            
            BlacklistEntry entry = blacklistService.addToBlacklist(type, value, reason, description, addedBy, merchantId, expiresAt);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully added to blacklist");
            response.put("entry", entry);
            
            log.info("Added to blacklist - Type: {}, Value: {}, ID: {}", type, value, entry.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error adding to blacklist: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add to blacklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Blacklist'ten entry kaldır (Business logic based - ID yerine type+value ile)
     */
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromBlacklist(@RequestBody Map<String, String> request) {
        try {
            String typeStr = request.get("type");
            String value = request.get("value");
            
            if (typeStr == null || typeStr.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Blacklist type is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (value == null || value.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Value is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            BlacklistEntry.BlacklistType type;
            try {
                type = BlacklistEntry.BlacklistType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid blacklist type. Valid values: " + 
                        java.util.Arrays.toString(BlacklistEntry.BlacklistType.values()));
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            boolean removed = blacklistService.removeFromBlacklist(type, value);
            
            Map<String, Object> response = new HashMap<>();
            if (removed) {
                response.put("success", true);
                response.put("message", "Successfully removed from blacklist");
                response.put("removedEntry", Map.of(
                        "type", type,
                        "value", value
                ));
                
                log.info("Removed from blacklist - Type: {}, Value: {}", type, value);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Blacklist entry not found or already inactive");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error removing from blacklist: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to remove from blacklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    

    
    /**
     * Type'a göre blacklist entries getir
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<BlacklistEntry>> getByType(@PathVariable String type) {
        try {
            BlacklistEntry.BlacklistType blacklistType = BlacklistEntry.BlacklistType.valueOf(type);
            List<BlacklistEntry> entries = blacklistService.getBlacklistByType(blacklistType);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            log.error("Error getting blacklist by type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
    
    /**
     * Merchant'a göre blacklist entries getir
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<BlacklistEntry>> getByMerchant(@PathVariable String merchantId) {
        try {
            List<BlacklistEntry> entries = blacklistService.getMerchantBlacklist(merchantId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            log.error("Error getting merchant blacklist: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
    
    /**
     * Value blacklisted mi kontrol et
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkBlacklist(@RequestBody Map<String, String> request) {
        try {
            BlacklistEntry.BlacklistType type = BlacklistEntry.BlacklistType.valueOf(request.get("type"));
            String value = request.get("value");
            
            boolean isBlacklisted = blacklistService.isBlacklistedByType(type, value);
            
            Map<String, Object> response = new HashMap<>();
            response.put("blacklisted", isBlacklisted);
            response.put("type", type);
            response.put("value", value);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking blacklist: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to check blacklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Blacklist istatistikleri
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            for (BlacklistEntry.BlacklistType type : BlacklistEntry.BlacklistType.values()) {
                long count = blacklistService.getBlacklistStats(type);
                stats.put(type.name().toLowerCase(), count);
            }
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting blacklist stats: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Expired entries cleanup
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpired() {
        try {
            blacklistService.cleanupExpiredEntries();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cleanup completed");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Cleanup failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Kart blacklist'e ekle (PCI DSS compliant - sadece BIN + Last4)
     */
    @PostMapping("/add-card")
    public ResponseEntity<Map<String, Object>> addCardToBlacklist(@RequestBody Map<String, Object> request) {
        try {
            String cardNumber = (String) request.get("cardNumber");
            BlacklistEntry.BlacklistReason reason = BlacklistEntry.BlacklistReason.valueOf((String) request.get("reason"));
            String description = (String) request.get("description");
            String addedBy = (String) request.get("addedBy");
            
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Card number is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            BlacklistEntry entry = blacklistService.addCardToBlacklist(cardNumber, reason, description, addedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Card successfully added to blacklist");
            response.put("entry", Map.of(
                    "id", entry.getId(),
                    "cardBin", entry.getCardBin(),
                    "lastFourDigits", entry.getLastFourDigits(),
                    "reason", entry.getReason(),
                    "description", entry.getDescription(),
                    "createdAt", entry.getCreatedAt()
            ));
            
            log.info("Card added to blacklist - BIN: {}, Last4: {}, Reason: {}", 
                    entry.getCardBin(), entry.getLastFourDigits(), reason);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid blacklist reason: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid reason. Valid values: " + java.util.Arrays.toString(BlacklistEntry.BlacklistReason.values()));
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error adding card to blacklist: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to add card to blacklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Kart blacklist'ten kaldır (PCI DSS compliant - BIN + Last4 ile)
     */
    @DeleteMapping("/remove-card")
    public ResponseEntity<Map<String, Object>> removeCardFromBlacklist(@RequestBody Map<String, String> request) {
        try {
            String cardBin = request.get("cardBin");
            String lastFour = request.get("lastFour");
            
            if (cardBin == null || cardBin.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Card BIN is required (first 6 digits)");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (lastFour == null || lastFour.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Last four digits are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (cardBin.length() != 6) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Card BIN must be exactly 6 digits");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (lastFour.length() != 4) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Last four digits must be exactly 4 digits");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            boolean removed = blacklistService.removeCardFromBlacklist(cardBin, lastFour);
            
            Map<String, Object> response = new HashMap<>();
            if (removed) {
                response.put("success", true);
                response.put("message", "Card successfully removed from blacklist");
                response.put("removedCard", Map.of(
                        "cardBin", cardBin,
                        "lastFour", lastFour
                ));
                
                log.info("Card removed from blacklist - BIN: {}, Last4: {}", cardBin, lastFour);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Card not found in blacklist or already inactive");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error removing card from blacklist: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to remove card from blacklist: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}

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
@RequestMapping("/api/v1/blacklist")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
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
     * Blacklist'ten entry kaldır
     */
    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Map<String, Object>> removeFromBlacklist(@PathVariable Long id) {
        try {
            boolean removed = blacklistService.removeFromBlacklist(id);
            
            Map<String, Object> response = new HashMap<>();
            if (removed) {
                response.put("success", true);
                response.put("message", "Successfully removed from blacklist");
                log.info("Removed from blacklist - ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Blacklist entry not found");
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
}

package com.payment.gateway.service;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.model.BlacklistEntry;
import com.payment.gateway.repository.BlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {
    
    private final BlacklistRepository blacklistRepository;
    private final AuditService auditService;
    
    public boolean isBlacklisted(PaymentRequest request) {
        log.debug("Checking blacklist for payment request");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Check card BIN + Last4 combination (PCI DSS compliant)
        if (isCardBinLast4Blacklisted(request.getCardNumber(), now)) {
            log.warn("Payment blocked - card BIN+Last4 is blacklisted: {} ending in {}", 
                    getCardBin(request.getCardNumber()), getLastFourDigits(request.getCardNumber()));
            return true;
        }
        
        // Check card BIN only
        if (isCardBinBlacklisted(request.getCardNumber(), now)) {
            log.warn("Payment blocked - card BIN is blacklisted: {}", getCardBin(request.getCardNumber()));
            return true;
        }
        
        log.debug("Blacklist check passed for payment request");
        return false;
    }
    
    public boolean isBlacklistedByType(BlacklistEntry.BlacklistType type, String value) {
        LocalDateTime now = LocalDateTime.now();
        Optional<BlacklistEntry> entry = blacklistRepository.findActiveEntry(type, value, now);
        return entry.isPresent();
    }
    
    private boolean isCardBlacklisted(String cardNumber, LocalDateTime now) {
        if (cardNumber == null) return false;
        
        // Check BIN + Last4 combination (new PCI DSS compliant method)
        return isCardBinLast4Blacklisted(cardNumber, now);
    }
    
    private boolean isCardBinLast4Blacklisted(String cardNumber, LocalDateTime now) {
        if (cardNumber == null || cardNumber.length() < 10) return false;
        
        String cardBin = getCardBin(cardNumber);
        String lastFour = getLastFourDigits(cardNumber);
        
        // Check if BIN+Last4 combination is blacklisted
        Optional<BlacklistEntry> entry = blacklistRepository.findActiveCardBinLast4Entry(cardBin, lastFour, now);
        
        return entry.isPresent();
    }
    
    private boolean isCardBinBlacklisted(String cardNumber, LocalDateTime now) {
        if (cardNumber == null || cardNumber.length() < 6) return false;
        
        // Check if any BIN patterns match
        List<BlacklistEntry> binEntries = blacklistRepository.findMatchingCardBins(cardNumber, now);
        
        if (!binEntries.isEmpty()) {
            log.debug("Found {} matching BIN blacklist entries", binEntries.size());
            return true;
        }
        
        return false;
    }
    
    @Transactional
    public BlacklistEntry addToBlacklist(BlacklistEntry.BlacklistType type, String value, 
                                        BlacklistEntry.BlacklistReason reason, String description,
                                        String addedBy, String merchantId, LocalDateTime expiresAt) {
        
        log.info("Adding to blacklist - Type: {}, Value: {}, Reason: {}", type, maskValue(type, value), reason);
        
        // Check if already exists
        Optional<BlacklistEntry> existing = blacklistRepository.findActiveEntry(type, value, LocalDateTime.now());
        if (existing.isPresent()) {
            log.warn("Entry already exists in blacklist: {} - {}", type, maskValue(type, value));
            return existing.get();
        }
        
        BlacklistEntry entry = new BlacklistEntry();
        entry.setType(type);
        entry.setValue(value);
        entry.setReason(reason);
        entry.setDescription(description);
        entry.setIsActive(true);
        entry.setExpiresAt(expiresAt);
        entry.setAddedBy(addedBy);
        entry.setMerchantId(merchantId);
        
        BlacklistEntry savedEntry = blacklistRepository.save(entry);
        
        // Audit log
        auditService.logBlacklist("ADDED", type.name(), maskValue(type, value), 
                                reason.name(), "api-user", null, savedEntry);
        
        log.info("Successfully added to blacklist - ID: {}, Type: {}", savedEntry.getId(), type);
        return savedEntry;
    }
    
    @Transactional
    public boolean removeFromBlacklist(BlacklistEntry.BlacklistType type, String value) {
        Optional<BlacklistEntry> entry = blacklistRepository.findByTypeAndValueAndIsActiveTrue(type, value);
        if (entry.isPresent()) {
            BlacklistEntry blacklistEntry = entry.get();
            BlacklistEntry oldEntry = new BlacklistEntry();
            // Copy for audit
            oldEntry.setId(blacklistEntry.getId());
            oldEntry.setType(blacklistEntry.getType());
            oldEntry.setValue(maskValue(type, value)); // Masked for audit
            oldEntry.setIsActive(blacklistEntry.getIsActive());
            
            blacklistEntry.setIsActive(false);
            blacklistRepository.save(blacklistEntry);
            
            // Audit log
            auditService.logBlacklist("REMOVED", type.name(), maskValue(type, value), 
                                    "MANUAL_REMOVAL", "system", oldEntry, blacklistEntry);
            
            log.info("Removed from blacklist - Type: {}, Value: {}", type, maskValue(type, value));
            return true;
        }
        
        log.warn("Blacklist entry not found for removal - Type: {}, Value: {}", type, maskValue(type, value));
        return false;
    }
    
    @Transactional
    public boolean removeCardFromBlacklist(String cardBin, String lastFour) {
        Optional<BlacklistEntry> entry = blacklistRepository.findActiveCardBinLast4Entry(cardBin, lastFour, LocalDateTime.now());
        if (entry.isPresent()) {
            BlacklistEntry blacklistEntry = entry.get();
            blacklistEntry.setIsActive(false);
            blacklistRepository.save(blacklistEntry);
            
            log.info("Removed card from blacklist - BIN: {}, Last4: {}", cardBin, lastFour);
            return true;
        }
        
        log.warn("Card not found in blacklist for removal - BIN: {}, Last4: {}", cardBin, lastFour);
        return false;
    }
    
    public List<BlacklistEntry> getBlacklistByType(BlacklistEntry.BlacklistType type) {
        return blacklistRepository.findByTypeAndIsActiveTrue(type);
    }
    
    public List<BlacklistEntry> getMerchantBlacklist(String merchantId) {
        return blacklistRepository.findActiveMerchantEntries(merchantId, LocalDateTime.now());
    }
    
    public List<BlacklistEntry> getBlacklistByReason(BlacklistEntry.BlacklistReason reason) {
        return blacklistRepository.findByReason(reason);
    }
    
    @Transactional
    public void cleanupExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        List<BlacklistEntry> allEntries = blacklistRepository.findAll();
        
        int cleanedUp = 0;
        for (BlacklistEntry entry : allEntries) {
            if (entry.getIsActive() && entry.getExpiresAt() != null && entry.getExpiresAt().isBefore(now)) {
                entry.setIsActive(false);
                blacklistRepository.save(entry);
                cleanedUp++;
            }
        }
        
        if (cleanedUp > 0) {
            log.info("Cleaned up {} expired blacklist entries", cleanedUp);
        }
    }
    
    // Convenience methods for common blacklist operations
    public BlacklistEntry addCardToBlacklist(String cardNumber, BlacklistEntry.BlacklistReason reason, 
                                           String description, String addedBy) {
        
        // Extract BIN and Last4 for PCI DSS compliance
        String cardBin = getCardBin(cardNumber);
        String lastFour = getLastFourDigits(cardNumber);
        
        BlacklistEntry entry = new BlacklistEntry();
        entry.setType(BlacklistEntry.BlacklistType.CARD_BIN_LAST4);
        entry.setValue(""); // Not used for card entries
        entry.setCardBin(cardBin);
        entry.setLastFourDigits(lastFour);
        entry.setReason(reason);
        entry.setDescription(description);
        entry.setIsActive(true);
        entry.setAddedBy(addedBy);
        
        BlacklistEntry savedEntry = blacklistRepository.save(entry);
        
        log.info("Successfully added card to blacklist - BIN: {}, Last4: {}, Reason: {}", 
                cardBin, lastFour, reason);
        
        return savedEntry;
    }
    
    public BlacklistEntry addEmailToBlacklist(String email, BlacklistEntry.BlacklistReason reason, 
                                            String description, String addedBy) {
        return addToBlacklist(BlacklistEntry.BlacklistType.EMAIL, email, reason, 
                            description, addedBy, null, null);
    }
    
    public BlacklistEntry addIpToBlacklist(String ipAddress, BlacklistEntry.BlacklistReason reason, 
                                         String description, String addedBy, LocalDateTime expiresAt) {
        return addToBlacklist(BlacklistEntry.BlacklistType.IP_ADDRESS, ipAddress, reason, 
                            description, addedBy, null, expiresAt);
    }
    
    public BlacklistEntry addCardBinToBlacklist(String cardBin, BlacklistEntry.BlacklistReason reason, 
                                              String description, String addedBy) {
        return addToBlacklist(BlacklistEntry.BlacklistType.CARD_BIN, cardBin, reason, 
                            description, addedBy, null, null);
    }
    
    private String getCardBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return "UNKNOWN";
        }
        return cardNumber.substring(0, 6);
    }
    
    private String getLastFourDigits(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "UNKNOWN";
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
    
    private String maskValue(BlacklistEntry.BlacklistType type, String value) {
        if (value == null) return "null";
        
        switch (type) {
            case CARD_BIN_LAST4:
                return "BIN+Last4"; // No sensitive data to mask
            case CARD_BIN:
                return value; // BIN is not sensitive
            case EMAIL:
                if (value.contains("@")) {
                    String[] parts = value.split("@");
                    return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
                }
                return "***";
            case IP_ADDRESS:
                String[] ipParts = value.split("\\.");
                if (ipParts.length == 4) {
                    return ipParts[0] + "." + ipParts[1] + ".***." + ipParts[3];
                }
                return "***";
            case PHONE:
                if (value.length() > 4) {
                    return "***" + value.substring(value.length() - 4);
                }
                return "***";
            default:
                return value.substring(0, Math.min(3, value.length())) + "***";
        }
    }
    
    public long getBlacklistStats(BlacklistEntry.BlacklistType type) {
        return blacklistRepository.countActiveEntriesByType(type);
    }
}

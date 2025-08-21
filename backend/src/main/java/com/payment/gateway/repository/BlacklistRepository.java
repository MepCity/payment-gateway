package com.payment.gateway.repository;

import com.payment.gateway.model.BlacklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlacklistRepository extends JpaRepository<BlacklistEntry, Long> {
    
    List<BlacklistEntry> findByType(BlacklistEntry.BlacklistType type);
    
    List<BlacklistEntry> findByTypeAndIsActiveTrue(BlacklistEntry.BlacklistType type);
    
    Optional<BlacklistEntry> findByTypeAndValueAndIsActiveTrue(BlacklistEntry.BlacklistType type, String value);
    
    List<BlacklistEntry> findByMerchantIdAndIsActiveTrue(String merchantId);
    
    List<BlacklistEntry> findByReason(BlacklistEntry.BlacklistReason reason);
    
    @Query("SELECT b FROM BlacklistEntry b WHERE b.type = :type AND b.value = :value AND b.isActive = true AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    Optional<BlacklistEntry> findActiveEntry(@Param("type") BlacklistEntry.BlacklistType type, 
                                           @Param("value") String value, 
                                           @Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM BlacklistEntry b WHERE b.type = 'CARD_BIN_LAST4' AND b.cardBin = :cardBin AND b.lastFourDigits = :lastFour AND b.isActive = true AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    Optional<BlacklistEntry> findActiveCardBinLast4Entry(@Param("cardBin") String cardBin, 
                                                        @Param("lastFour") String lastFour, 
                                                        @Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM BlacklistEntry b WHERE b.type = 'CARD_BIN' AND :cardNumber LIKE CONCAT(b.value, '%') AND b.isActive = true AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    List<BlacklistEntry> findMatchingCardBins(@Param("cardNumber") String cardNumber, @Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM BlacklistEntry b WHERE b.merchantId = :merchantId AND b.isActive = true AND (b.expiresAt IS NULL OR b.expiresAt > :now)")
    List<BlacklistEntry> findActiveMerchantEntries(@Param("merchantId") String merchantId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(b) FROM BlacklistEntry b WHERE b.type = :type AND b.isActive = true")
    long countActiveEntriesByType(@Param("type") BlacklistEntry.BlacklistType type);
}

package com.payment.gateway.repository;

import com.payment.gateway.model.VelocityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VelocityCheckRepository extends JpaRepository<VelocityCheck, Long> {
    
    List<VelocityCheck> findByType(VelocityCheck.VelocityType type);
    
    List<VelocityCheck> findByIdentifier(String identifier);
    
    List<VelocityCheck> findByMerchantId(String merchantId);
    
    List<VelocityCheck> findByLimitExceededTrue();
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.cardNumber LIKE CONCAT(:cardPrefix, '%') AND p.createdAt >= :since AND p.createdAt <= :until AND p.status != 'FAILED'")
    long countCardTransactions(@Param("cardPrefix") String cardPrefix, 
                              @Param("since") LocalDateTime since, 
                              @Param("until") LocalDateTime until);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.cardNumber LIKE CONCAT(:cardPrefix, '%') AND p.createdAt >= :since AND p.createdAt <= :until AND p.status = 'COMPLETED'")
    BigDecimal sumCardTransactionAmount(@Param("cardPrefix") String cardPrefix, 
                                       @Param("since") LocalDateTime since, 
                                       @Param("until") LocalDateTime until);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.customerId = :customerId AND p.createdAt >= :since AND p.createdAt <= :until AND p.status != 'FAILED'")
    long countCustomerTransactions(@Param("customerId") String customerId, 
                                  @Param("since") LocalDateTime since, 
                                  @Param("until") LocalDateTime until);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.merchantId = :merchantId AND p.createdAt >= :since AND p.createdAt <= :until AND p.status != 'FAILED'")
    long countMerchantTransactions(@Param("merchantId") String merchantId, 
                                  @Param("since") LocalDateTime since, 
                                  @Param("until") LocalDateTime until);
    
    @Query("SELECT v FROM VelocityCheck v WHERE v.identifier = :identifier AND v.type = :type AND v.checkedAt >= :since ORDER BY v.checkedAt DESC")
    List<VelocityCheck> findRecentChecks(@Param("identifier") String identifier, 
                                        @Param("type") VelocityCheck.VelocityType type, 
                                        @Param("since") LocalDateTime since);
}
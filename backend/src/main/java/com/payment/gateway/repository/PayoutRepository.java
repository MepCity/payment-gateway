package com.payment.gateway.repository;

import com.payment.gateway.model.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, Long> {
    
    Optional<Payout> findByPayoutId(String payoutId);
    
    Optional<Payout> findByPaymentId(String paymentId);
    
    List<Payout> findByMerchantId(String merchantId);
    
    List<Payout> findByCustomerId(String customerId);
    
    List<Payout> findByStatus(Payout.PayoutStatus status);
    
    List<Payout> findByType(Payout.PayoutType type);
    
    List<Payout> findByMerchantIdAndStatus(String merchantId, Payout.PayoutStatus status);
    
    List<Payout> findByCustomerIdAndStatus(String customerId, Payout.PayoutStatus status);
    
    List<Payout> findByMerchantIdAndType(String merchantId, Payout.PayoutType type);
    
    List<Payout> findByCustomerIdAndType(String customerId, Payout.PayoutType type);
    
    @Query("SELECT p FROM Payout p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payout> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payout p WHERE p.merchantId = :merchantId AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payout> findByMerchantIdAndDateRange(@Param("merchantId") String merchantId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payout p WHERE p.customerId = :customerId AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payout> findByCustomerIdAndDateRange(@Param("customerId") String customerId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.status = :status")
    long countByStatus(@Param("status") Payout.PayoutStatus status);
    
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.merchantId = :merchantId AND p.status = :status")
    long countByMerchantIdAndStatus(@Param("merchantId") String merchantId, @Param("status") Payout.PayoutStatus status);
    
    @Query("SELECT COUNT(p) FROM Payout p WHERE p.customerId = :customerId AND p.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") String customerId, @Param("status") Payout.PayoutStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payout p WHERE p.status = :status AND p.merchantId = :merchantId")
    BigDecimal sumAmountByStatusAndMerchantId(@Param("status") Payout.PayoutStatus status, @Param("merchantId") String merchantId);
    
    @Query("SELECT SUM(p.amount) FROM Payout p WHERE p.status = :status AND p.customerId = :customerId")
    BigDecimal sumAmountByStatusAndCustomerId(@Param("status") Payout.PayoutStatus status, @Param("customerId") String customerId);
    
    @Query("SELECT SUM(p.amount) FROM Payout p WHERE p.merchantId = :merchantId AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByMerchantIdAndDateRange(@Param("merchantId") String merchantId, 
                                                @Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(p.amount) FROM Payout p WHERE p.customerId = :customerId AND p.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByCustomerIdAndDateRange(@Param("customerId") String customerId, 
                                                @Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);
    
    boolean existsByPayoutId(String payoutId);
    
    boolean existsByGatewayPayoutId(String gatewayPayoutId);
    
    @Query("SELECT p FROM Payout p WHERE p.bankAccountNumber LIKE %:accountNumber%")
    List<Payout> findByBankAccountNumberContaining(@Param("accountNumber") String accountNumber);
    
    @Query("SELECT p FROM Payout p WHERE p.accountHolderName LIKE %:holderName%")
    List<Payout> findByAccountHolderNameContaining(@Param("holderName") String holderName);
    
    @Query("SELECT p FROM Payout p WHERE p.bankName LIKE %:bankName%")
    List<Payout> findByBankNameContaining(@Param("bankName") String bankName);
}
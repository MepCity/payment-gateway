package com.payment.gateway.repository;

import com.payment.gateway.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    List<Payment> findByMerchantId(String merchantId);
    
    List<Payment> findByCustomerId(String customerId);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    List<Payment> findByMerchantIdAndStatus(String merchantId, Payment.PaymentStatus status);
    
    List<Payment> findByCustomerIdAndStatus(String customerId, Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.merchantId = :merchantId AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    long countByStatus(@Param("status") Payment.PaymentStatus status);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.merchantId = :merchantId")
    Double sumAmountByStatusAndMerchantId(@Param("status") Payment.PaymentStatus status,
                                         @Param("merchantId") String merchantId);
    
    boolean existsByTransactionId(String transactionId);
    
    boolean existsByGatewayTransactionId(String gatewayTransactionId);
}

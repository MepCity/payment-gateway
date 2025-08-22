package com.payment.gateway.repository;

import com.payment.gateway.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    Optional<Refund> findByRefundId(String refundId);
    
    Optional<Refund> findByPaymentId(String paymentId);
    
    Optional<Refund> findByGatewayRefundId(String gatewayRefundId);
    
    List<Refund> findByTransactionId(String transactionId);
    
    List<Refund> findByMerchantId(String merchantId);
    
    List<Refund> findByCustomerId(String customerId);
    
    List<Refund> findByStatus(Refund.RefundStatus status);
    
    List<Refund> findByReason(Refund.RefundReason reason);
    
    List<Refund> findByMerchantIdAndStatus(String merchantId, Refund.RefundStatus status);
    
    List<Refund> findByCustomerIdAndStatus(String customerId, Refund.RefundStatus status);
    
    // Scheduler için PROCESSING durumundaki eski refund'ları bul
    List<Refund> findByStatusAndCreatedAtBefore(Refund.RefundStatus status, LocalDateTime createdAt);
    
    @Query("SELECT r FROM Refund r WHERE r.refundDate BETWEEN :startDate AND :endDate")
    List<Refund> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r FROM Refund r WHERE r.merchantId = :merchantId AND r.refundDate BETWEEN :startDate AND :endDate")
    List<Refund> findByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = :status")
    long countByStatus(@Param("status") Refund.RefundStatus status);
    
    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.status = :status AND r.merchantId = :merchantId")
    Double sumAmountByStatusAndMerchantId(@Param("status") Refund.RefundStatus status,
                                         @Param("merchantId") String merchantId);
    
    boolean existsByRefundId(String refundId);
    
    boolean existsByGatewayRefundId(String gatewayRefundId);
    
    // Merchant-aware finder methods for data isolation
    Optional<Refund> findByRefundIdAndMerchantId(String refundId, String merchantId);
    
    Optional<Refund> findByPaymentIdAndMerchantId(String paymentId, String merchantId);
    
    List<Refund> findByCustomerIdAndMerchantId(String customerId, String merchantId);
    
    List<Refund> findByStatusAndMerchantId(Refund.RefundStatus status, String merchantId);
    
    List<Refund> findByReasonAndMerchantId(Refund.RefundReason reason, String merchantId);
    
    List<Refund> findByTransactionIdAndMerchantId(String transactionId, String merchantId);
    
    // Scheduler için uzun süredir pending olan refund'ları bul
    @Query("SELECT r FROM Refund r WHERE r.status = 'PROCESSING' AND r.createdAt < :cutoffTime")
    List<Refund> findLongPendingRefunds(@Param("cutoffTime") LocalDateTime cutoffTime);
}
package com.payment.gateway.repository;

import com.payment.gateway.model.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    
    Optional<Dispute> findByDisputeId(String disputeId);
    
    Optional<Dispute> findByPaymentId(String paymentId);
    
    List<Dispute> findByTransactionId(String transactionId);
    
    List<Dispute> findByMerchantId(String merchantId);
    
    List<Dispute> findByCustomerId(String customerId);
    
    List<Dispute> findByStatus(Dispute.DisputeStatus status);
    
    List<Dispute> findByReason(Dispute.DisputeReason reason);
    
    List<Dispute> findByMerchantIdAndStatus(String merchantId, Dispute.DisputeStatus status);
    
    List<Dispute> findByCustomerIdAndStatus(String customerId, Dispute.DisputeStatus status);
    
    @Query("SELECT d FROM Dispute d WHERE d.disputeDate BETWEEN :startDate AND :endDate")
    List<Dispute> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT d FROM Dispute d WHERE d.merchantId = :merchantId AND d.disputeDate BETWEEN :startDate AND :endDate")
    List<Dispute> findByMerchantIdAndDateRange(@Param("merchantId") String merchantId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = :status")
    long countByStatus(@Param("status") Dispute.DisputeStatus status);
    
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = :status AND d.merchantId = :merchantId")
    long countByStatusAndMerchantId(@Param("status") Dispute.DisputeStatus status,
                                    @Param("merchantId") String merchantId);
    
    boolean existsByDisputeId(String disputeId);
    
    boolean existsByGatewayDisputeId(String gatewayDisputeId);
    
    // Yeni bank dispute metodları
    Optional<Dispute> findByBankDisputeId(String bankDisputeId);
    
    List<Dispute> findByMerchantIdAndStatusIn(String merchantId, List<Dispute.DisputeStatus> statuses);
    
    List<Dispute> findByBankName(String bankName);
    
    @Query("SELECT d FROM Dispute d WHERE d.merchantResponseDeadline < :currentTime AND d.status IN :pendingStatuses")
    List<Dispute> findExpiredMerchantResponses(@Param("currentTime") LocalDateTime currentTime,
                                               @Param("pendingStatuses") List<Dispute.DisputeStatus> pendingStatuses);
    
    @Query("SELECT d FROM Dispute d WHERE d.merchantResponseDeadline BETWEEN :startTime AND :endTime AND d.status IN :pendingStatuses")
    List<Dispute> findUpcomingDeadlines(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("pendingStatuses") List<Dispute.DisputeStatus> pendingStatuses);
    
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.merchantId = :merchantId AND d.status = 'BANK_APPROVED'")
    long countWonDisputesByMerchant(@Param("merchantId") String merchantId);
    
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.merchantId = :merchantId AND d.status = 'BANK_REJECTED'")
    long countLostDisputesByMerchant(@Param("merchantId") String merchantId);
    
    // Scheduler için deadline kontrol metodları
    @Query("SELECT d FROM Dispute d WHERE d.merchantResponseDeadline < :currentTime AND d.status = 'PENDING_MERCHANT_RESPONSE'")
    List<Dispute> findDisputesWithExpiredResponseDeadline(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT d FROM Dispute d WHERE d.adminEvaluationDeadline < :currentTime AND d.status = 'PENDING_ADMIN_EVALUATION'")
    List<Dispute> findDisputesWithExpiredAdminDeadline(@Param("currentTime") LocalDateTime currentTime);
    
    // İstatistik metodları
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.disputeDate BETWEEN :startDate AND :endDate")
    long countDisputesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = 'PENDING_MERCHANT_RESPONSE'")
    long countPendingMerchantResponses();
    
    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.status = 'PENDING_ADMIN_EVALUATION'")
    long countPendingAdminEvaluations();
}
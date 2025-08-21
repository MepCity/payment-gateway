package com.payment.gateway.repository;

import com.payment.gateway.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    
    Optional<RiskAssessment> findByAssessmentId(String assessmentId);
    
    Optional<RiskAssessment> findByPaymentId(String paymentId);
    
    List<RiskAssessment> findByMerchantId(String merchantId);
    
    List<RiskAssessment> findByCustomerId(String customerId);
    
    List<RiskAssessment> findByRiskLevel(RiskAssessment.RiskLevel riskLevel);
    
    List<RiskAssessment> findByAction(RiskAssessment.AssessmentAction action);
    
    @Query("SELECT r FROM RiskAssessment r WHERE r.ipAddress = :ipAddress AND r.assessedAt >= :since")
    List<RiskAssessment> findByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    @Query("SELECT r FROM RiskAssessment r WHERE r.merchantId = :merchantId AND r.riskLevel IN :riskLevels AND r.assessedAt >= :since")
    List<RiskAssessment> findHighRiskAssessments(@Param("merchantId") String merchantId, 
                                                @Param("riskLevels") List<RiskAssessment.RiskLevel> riskLevels,
                                                @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(r) FROM RiskAssessment r WHERE r.customerId = :customerId AND r.assessedAt >= :since")
    long countByCustomerIdSince(@Param("customerId") String customerId, @Param("since") LocalDateTime since);
    
    @Query("SELECT AVG(r.riskScore) FROM RiskAssessment r WHERE r.merchantId = :merchantId AND r.assessedAt >= :since")
    Double getAverageRiskScoreByMerchant(@Param("merchantId") String merchantId, @Param("since") LocalDateTime since);
}
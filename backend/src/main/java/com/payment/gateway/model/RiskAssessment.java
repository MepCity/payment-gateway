package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "risk_assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String assessmentId;
    
    @Column(nullable = false)
    private String paymentId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore; // 0.00 - 100.00
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column
    private String userAgent;
    
    @Column
    private String deviceFingerprint;
    
    @Column
    private String geolocation;
    
    @Column(length = 2000)
    private String riskFactors; // JSON array of risk factors
    
    @Column(length = 1000)
    private String recommendation;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentAction action;
    
    @Column
    private String velocityCheckResult;
    
    @Column
    private String blacklistCheckResult;
    
    @Column
    private String cardBinCheckResult;
    
    @Column
    private String amountRiskResult;
    
    @CreationTimestamp
    @Column(name = "assessed_at", nullable = false, updatable = false)
    private LocalDateTime assessedAt;
    
    public enum RiskLevel {
        LOW,        // 0-30: Safe transaction
        MEDIUM,     // 31-70: Review required
        HIGH,       // 71-90: High risk, manual review
        CRITICAL    // 91-100: Block transaction
    }
    
    public enum AssessmentAction {
        APPROVE,        // Allow transaction
        REVIEW,         // Manual review required
        CHALLENGE,      // Additional verification needed
        DECLINE         // Block transaction
    }
}

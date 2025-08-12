package com.payment.gateway.service;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.model.Payment;
import com.payment.gateway.model.RiskAssessment;
import com.payment.gateway.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentService {
    
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final VelocityCheckService velocityCheckService;
    private final BlacklistService blacklistService;
    
    @Transactional
    public RiskAssessment assessPaymentRisk(PaymentRequest request, Payment payment, String ipAddress, String userAgent) {
        log.info("Starting risk assessment for payment: {}", payment.getPaymentId());
        
        RiskAssessment assessment = new RiskAssessment();
        assessment.setAssessmentId(generateAssessmentId());
        assessment.setPaymentId(payment.getPaymentId());
        assessment.setMerchantId(payment.getMerchantId());
        assessment.setCustomerId(payment.getCustomerId());
        assessment.setIpAddress(ipAddress);
        assessment.setUserAgent(userAgent);
        
        List<String> riskFactors = new ArrayList<>();
        BigDecimal riskScore = BigDecimal.ZERO;
        
        // 1. Amount Risk Check
        BigDecimal amountRisk = assessAmountRisk(request.getAmount());
        riskScore = riskScore.add(amountRisk);
        if (amountRisk.compareTo(BigDecimal.valueOf(20)) > 0) {
            riskFactors.add("HIGH_AMOUNT: " + request.getAmount());
        }
        assessment.setAmountRiskResult("Amount risk score: " + amountRisk);
        
        // 2. Velocity Checks
        boolean velocityExceeded = velocityCheckService.checkVelocityLimits(request, ipAddress);
        if (velocityExceeded) {
            riskScore = riskScore.add(BigDecimal.valueOf(30));
            riskFactors.add("VELOCITY_EXCEEDED");
        }
        assessment.setVelocityCheckResult(velocityExceeded ? "FAILED" : "PASSED");
        
        // 3. Blacklist Checks
        boolean isBlacklisted = blacklistService.isBlacklisted(request);
        if (isBlacklisted) {
            riskScore = riskScore.add(BigDecimal.valueOf(50));
            riskFactors.add("BLACKLISTED");
        }
        assessment.setBlacklistCheckResult(isBlacklisted ? "FAILED" : "PASSED");
        
        // 4. Card BIN Risk Check
        BigDecimal binRisk = assessCardBinRisk(request.getCardNumber());
        riskScore = riskScore.add(binRisk);
        if (binRisk.compareTo(BigDecimal.valueOf(15)) > 0) {
            riskFactors.add("HIGH_RISK_BIN");
        }
        assessment.setCardBinCheckResult("BIN risk score: " + binRisk);
        
        // 5. Time-based Risk Check
        BigDecimal timeRisk = assessTimeRisk();
        riskScore = riskScore.add(timeRisk);
        if (timeRisk.compareTo(BigDecimal.valueOf(10)) > 0) {
            riskFactors.add("OFF_HOURS_TRANSACTION");
        }
        
        // 6. Geographic Risk (basic implementation)
        BigDecimal geoRisk = assessGeographicRisk(ipAddress);
        riskScore = riskScore.add(geoRisk);
        if (geoRisk.compareTo(BigDecimal.valueOf(15)) > 0) {
            riskFactors.add("HIGH_RISK_LOCATION");
        }
        
        // Cap risk score at 100
        if (riskScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            riskScore = BigDecimal.valueOf(100);
        }
        
        assessment.setRiskScore(riskScore.setScale(2, RoundingMode.HALF_UP));
        assessment.setRiskLevel(determineRiskLevel(riskScore));
        assessment.setAction(determineAction(riskScore, riskFactors));
        assessment.setRiskFactors(String.join(", ", riskFactors));
        assessment.setRecommendation(generateRecommendation(assessment));
        
        RiskAssessment savedAssessment = riskAssessmentRepository.save(assessment);
        
        log.info("Risk assessment completed for payment: {} - Risk Level: {}, Score: {}", 
                payment.getPaymentId(), assessment.getRiskLevel(), assessment.getRiskScore());
        
        return savedAssessment;
    }
    
    private BigDecimal assessAmountRisk(BigDecimal amount) {
        // Risk increases with amount
        if (amount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return BigDecimal.valueOf(25); // Very high amount
        } else if (amount.compareTo(BigDecimal.valueOf(5000)) > 0) {
            return BigDecimal.valueOf(20); // High amount
        } else if (amount.compareTo(BigDecimal.valueOf(1000)) > 0) {
            return BigDecimal.valueOf(10); // Medium amount
        } else if (amount.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(5); // Normal amount
        }
        return BigDecimal.ZERO; // Low amount
    }
    
    private BigDecimal assessCardBinRisk(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return BigDecimal.valueOf(20); // Unknown BIN is risky
        }
        
        String bin = cardNumber.substring(0, 6);
        
        // High-risk BINs (example - in production, use real data)
        List<String> highRiskBins = List.of("555555", "444444", "666666");
        if (highRiskBins.contains(bin)) {
            return BigDecimal.valueOf(25);
        }
        
        // Check if it's a known good BIN (major banks)
        List<String> trustedBins = List.of("482494", "540061", "454360"); // Garanti BBVA
        if (trustedBins.contains(bin)) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(5); // Unknown BIN, slight risk
    }
    
    private BigDecimal assessTimeRisk() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        
        // Higher risk during off-hours (22:00 - 06:00)
        if (hour >= 22 || hour <= 6) {
            return BigDecimal.valueOf(15);
        }
        
        // Medium risk during early morning (06:00 - 09:00)
        if (hour >= 6 && hour <= 9) {
            return BigDecimal.valueOf(5);
        }
        
        return BigDecimal.ZERO; // Normal business hours
    }
    
    private BigDecimal assessGeographicRisk(String ipAddress) {
        // Basic implementation - in production, use IP geolocation service
        if (ipAddress == null) {
            return BigDecimal.valueOf(10);
        }
        
        // Local/private IPs
        if (ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || 
            ipAddress.startsWith("127.") || ipAddress.equals("localhost")) {
            return BigDecimal.valueOf(5); // Slightly risky for production
        }
        
        // In production, check against:
        // - High-risk countries
        // - VPN/Proxy detection
        // - Distance from merchant location
        
        return BigDecimal.valueOf(2); // Default minimal risk
    }
    
    private RiskAssessment.RiskLevel determineRiskLevel(BigDecimal riskScore) {
        if (riskScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return RiskAssessment.RiskLevel.CRITICAL;
        } else if (riskScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return RiskAssessment.RiskLevel.HIGH;
        } else if (riskScore.compareTo(BigDecimal.valueOf(30)) >= 0) {
            return RiskAssessment.RiskLevel.MEDIUM;
        }
        return RiskAssessment.RiskLevel.LOW;
    }
    
    private RiskAssessment.AssessmentAction determineAction(BigDecimal riskScore, List<String> riskFactors) {
        // Critical risk - always decline
        if (riskScore.compareTo(BigDecimal.valueOf(90)) >= 0) {
            return RiskAssessment.AssessmentAction.DECLINE;
        }
        
        // Blacklisted - always decline
        if (riskFactors.contains("BLACKLISTED")) {
            return RiskAssessment.AssessmentAction.DECLINE;
        }
        
        // High risk - manual review
        if (riskScore.compareTo(BigDecimal.valueOf(70)) >= 0) {
            return RiskAssessment.AssessmentAction.REVIEW;
        }
        
        // Medium risk - challenge (3D Secure, etc.)
        if (riskScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return RiskAssessment.AssessmentAction.CHALLENGE;
        }
        
        // Low risk - approve
        return RiskAssessment.AssessmentAction.APPROVE;
    }
    
    private String generateRecommendation(RiskAssessment assessment) {
        switch (assessment.getAction()) {
            case APPROVE:
                return "Transaction appears safe. Proceed with normal processing.";
            case CHALLENGE:
                return "Medium risk detected. Recommend additional verification (3D Secure, SMS OTP).";
            case REVIEW:
                return "High risk transaction. Manual review recommended before processing.";
            case DECLINE:
                return "Critical risk or blacklisted entity. Decline transaction immediately.";
            default:
                return "Risk assessment completed.";
        }
    }
    
    private String generateAssessmentId() {
        return "RISK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public RiskAssessment getAssessmentByPaymentId(String paymentId) {
        return riskAssessmentRepository.findByPaymentId(paymentId).orElse(null);
    }
    
    public List<RiskAssessment> getHighRiskAssessments(String merchantId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<RiskAssessment.RiskLevel> highRiskLevels = List.of(
            RiskAssessment.RiskLevel.HIGH, 
            RiskAssessment.RiskLevel.CRITICAL
        );
        return riskAssessmentRepository.findHighRiskAssessments(merchantId, highRiskLevels, since);
    }
}

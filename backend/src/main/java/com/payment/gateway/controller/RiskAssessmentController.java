package com.payment.gateway.controller;

import com.payment.gateway.model.RiskAssessment;
import com.payment.gateway.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Risk Assessment Controller
 * Fraud detection risk değerlendirme sonuçları
 */
@RestController
@RequestMapping("/api/v1/risk")
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentController {
    
    private final RiskAssessmentService riskAssessmentService;
    
    /**
     * Payment ID'ye göre risk assessment getir
     */
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<RiskAssessment> getByPaymentId(@PathVariable String paymentId) {
        try {
            RiskAssessment assessment = riskAssessmentService.getAssessmentByPaymentId(paymentId);
            if (assessment != null) {
                return ResponseEntity.ok(assessment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting risk assessment for payment {}: {}", paymentId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Merchant'ın high risk assessments getir
     */
    @GetMapping("/assessments/{merchantId}")
    public ResponseEntity<List<RiskAssessment>> getHighRiskAssessments(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            List<RiskAssessment> assessments = riskAssessmentService.getHighRiskAssessments(merchantId, days);
            return ResponseEntity.ok(assessments);
        } catch (Exception e) {
            log.error("Error getting high risk assessments for merchant {}: {}", merchantId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Risk assessment istatistikleri
     */
    @GetMapping("/stats/{merchantId}")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            List<RiskAssessment> highRiskAssessments = riskAssessmentService.getHighRiskAssessments(merchantId, days);
            
            // Risk level dağılımı
            Map<String, Long> riskLevelDistribution = new HashMap<>();
            Map<String, Long> actionDistribution = new HashMap<>();
            
            for (RiskAssessment assessment : highRiskAssessments) {
                String riskLevel = assessment.getRiskLevel().name();
                String action = assessment.getAction().name();
                
                riskLevelDistribution.put(riskLevel, riskLevelDistribution.getOrDefault(riskLevel, 0L) + 1);
                actionDistribution.put(action, actionDistribution.getOrDefault(action, 0L) + 1);
            }
            
            stats.put("merchantId", merchantId);
            stats.put("period", days + " days");
            stats.put("totalHighRiskAssessments", highRiskAssessments.size());
            stats.put("riskLevelDistribution", riskLevelDistribution);
            stats.put("actionDistribution", actionDistribution);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting risk stats for merchant {}: {}", merchantId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

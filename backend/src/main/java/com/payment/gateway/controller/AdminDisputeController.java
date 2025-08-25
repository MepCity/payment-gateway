package com.payment.gateway.controller;

import com.payment.gateway.dto.AdminEvaluationRequest;
import com.payment.gateway.dto.DisputeResponse;
import com.payment.gateway.service.DisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/disputes")
@RequiredArgsConstructor
@Slf4j
public class AdminDisputeController {

    private final DisputeService disputeService;

    /**
     * Admin için tüm dispute'ları listeler
     */
    @GetMapping
    public ResponseEntity<List<DisputeResponse>> getAllDisputes() {
        try {
            List<DisputeResponse> disputes = disputeService.getAllDisputes();
            return ResponseEntity.ok(disputes);
        } catch (Exception e) {
            log.error("Error fetching all disputes for admin: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Belirli merchant'ın dispute'larını listele
     */
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByMerchant(@PathVariable String merchantId) {
        try {
            List<DisputeResponse> disputes = disputeService.getDisputesByMerchantId(merchantId);
            return ResponseEntity.ok(disputes);
        } catch (Exception e) {
            log.error("Error fetching disputes for merchant {}: {}", merchantId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Admin değerlendirmesi bekleyen dispute'ları listeler
     */
    @GetMapping("/pending-evaluation")
    public ResponseEntity<List<DisputeResponse>> getPendingEvaluationDisputes() {
        try {
            List<DisputeResponse> disputes = disputeService.getDisputesByStatus(
                com.payment.gateway.model.Dispute.DisputeStatus.PENDING_ADMIN_EVALUATION);
            return ResponseEntity.ok(disputes);
        } catch (Exception e) {
            log.error("Error fetching pending evaluation disputes: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Belirli bir dispute'ı getirir
     */
    @GetMapping("/{disputeId}")
    public ResponseEntity<DisputeResponse> getDispute(@PathVariable Long disputeId) {
        try {
            DisputeResponse dispute = disputeService.getDisputeById(disputeId);
            if (dispute.isSuccess()) {
                return ResponseEntity.ok(dispute);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error fetching dispute {} for admin: {}", disputeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Admin değerlendirmesi gönderir
     */
    @PostMapping("/{disputeId}/evaluate")
    public ResponseEntity<DisputeResponse> evaluateDispute(
            @PathVariable Long disputeId,
            @Valid @RequestBody AdminEvaluationRequest request) {
        try {
            log.info("Admin evaluating dispute {}: {}", disputeId, request.getDecision());
            
            DisputeResponse response = disputeService.processAdminEvaluation(disputeId, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error processing admin evaluation for dispute {}: {}", disputeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Dispute'ı manuel olarak approve eder (merchant favor)
     */
    @PostMapping("/{disputeId}/approve-merchant")
    public ResponseEntity<DisputeResponse> approveMerchant(@PathVariable Long disputeId) {
        try {
            log.info("Admin approving merchant for dispute {}", disputeId);
            
            AdminEvaluationRequest request = new AdminEvaluationRequest();
            request.setDecision("APPROVE_MERCHANT");
            request.setNotes("Manual admin approval for merchant");
            
            DisputeResponse response = disputeService.processAdminEvaluation(disputeId, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error approving merchant for dispute {}: {}", disputeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Dispute'ı manuel olarak approve eder (customer favor)
     */
    @PostMapping("/{disputeId}/approve-customer")
    public ResponseEntity<DisputeResponse> approveCustomer(@PathVariable Long disputeId) {
        try {
            log.info("Admin approving customer for dispute {}", disputeId);
            
            AdminEvaluationRequest request = new AdminEvaluationRequest();
            request.setDecision("APPROVE_CUSTOMER");
            request.setNotes("Manual admin approval for customer");
            
            DisputeResponse response = disputeService.processAdminEvaluation(disputeId, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error approving customer for dispute {}: {}", disputeId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Dispute istatistiklerini getirir
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getDisputeStatistics() {
        try {
            // Burada dispute statistics service'ini çağırabiliriz
            return ResponseEntity.ok("Dispute statistics endpoint - to be implemented");
        } catch (Exception e) {
            log.error("Error fetching dispute statistics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
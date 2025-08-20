package com.payment.gateway.controller;

import com.payment.gateway.dto.RefundRequest;
import com.payment.gateway.dto.RefundResponse;
import com.payment.gateway.dto.SimulateWebhookRequest;
import com.payment.gateway.model.Refund;
import com.payment.gateway.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/refunds")
@RequiredArgsConstructor
@Slf4j
public class RefundController {
    
    private final RefundService refundService;
    
    // POST - Create new refund
    @PostMapping
    public ResponseEntity<RefundResponse> createRefund(@Valid @RequestBody RefundRequest request) {
        log.info("Creating new refund for payment: {}, transaction: {}", 
                request.getPaymentId(), request.getTransactionId());
        
        RefundResponse response = refundService.createRefund(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // PUT - Complete/Approve a refund manually (for admin dashboard)
    @PutMapping("/{refundId}/complete")
    public ResponseEntity<RefundResponse> completeRefund(@PathVariable String refundId) {
        log.info("Manually completing refund: {}", refundId);
        
        RefundResponse response = refundService.completeRefund(refundId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // PUT - Cancel/Reject a refund manually
    @PutMapping("/{refundId}/cancel")
    public ResponseEntity<RefundResponse> cancelRefund(@PathVariable String refundId) {
        log.info("Manually cancelling refund: {}", refundId);
        
        RefundResponse response = refundService.cancelRefund(refundId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // GET - Get refund by ID
    @GetMapping("/{id}")
    public ResponseEntity<RefundResponse> getRefundById(@PathVariable Long id) {
        log.info("Retrieving refund with ID: {}", id);
        
        RefundResponse response = refundService.getRefundById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get refund by refund ID
    @GetMapping("/refund-id/{refundId}")
    public ResponseEntity<RefundResponse> getRefundByRefundId(@PathVariable String refundId) {
        log.info("Retrieving refund with refund ID: {}", refundId);
        
        RefundResponse response = refundService.getRefundByRefundId(refundId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get refund by payment ID
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<RefundResponse> getRefundByPaymentId(@PathVariable String paymentId) {
        log.info("Retrieving refund with payment ID: {}", paymentId);
        
        RefundResponse response = refundService.getRefundByPaymentId(paymentId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Banka'dan gelen refund webhook'ını simüle et (test için)
     */
    @PostMapping("/{refundId}/approve")
    public ResponseEntity<Map<String, Object>> approveRefund(
            @PathVariable String refundId,
            @RequestParam(defaultValue = "SUCCESS") String status,
            @RequestParam(defaultValue = "GARANTI") String bankType) {
        
        log.info("✅ Approving refund: {} - Status: {} - Bank: {}", refundId, status, bankType);
        
        try {
            // Webhook data formatı: gatewayRefundId|status|message
            String webhookData = refundId + "|" + status + "|" + bankType + " refund approved successfully";
            
            // RefundService'deki webhook processing metodunu çağır
            refundService.processBankRefundWebhook(bankType, webhookData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Refund approved successfully");
            result.put("refundId", refundId);
            result.put("status", status);
            result.put("bankType", bankType);
            result.put("webhookData", webhookData);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error simulating bank webhook for refund: {}", refundId, e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "Failed to simulate bank webhook: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
    
    // GET - Get all refunds
    @GetMapping
    public ResponseEntity<List<RefundResponse>> getAllRefunds() {
        log.info("Retrieving all refunds");
        
        List<RefundResponse> refunds = refundService.getAllRefunds();
        return ResponseEntity.ok(refunds);
    }
    
    // GET - Get refunds by merchant ID
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByMerchantId(@PathVariable String merchantId) {
        log.info("Retrieving refunds for merchant: {}", merchantId);
        
        List<RefundResponse> refunds = refundService.getRefundsByMerchantId(merchantId);
        return ResponseEntity.ok(refunds);
    }
    
    // GET - Get refunds by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByCustomerId(@PathVariable String customerId) {
        log.info("Retrieving refunds for customer: {}", customerId);
        
        List<RefundResponse> refunds = refundService.getRefundsByCustomerId(customerId);
        return ResponseEntity.ok(refunds);
    }
    
    // GET - Get refunds by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RefundResponse>> getRefundsByStatus(@PathVariable Refund.RefundStatus status) {
        log.info("Retrieving refunds with status: {}", status);
        
        List<RefundResponse> refunds = refundService.getRefundsByStatus(status);
        return ResponseEntity.ok(refunds);
    }
    
    // GET - Get refunds by reason
    @GetMapping("/reason/{reason}")
    public ResponseEntity<List<RefundResponse>> getRefundsByReason(@PathVariable Refund.RefundReason reason) {
        log.info("Retrieving refunds with reason: {}", reason);
        
        List<RefundResponse> refunds = refundService.getRefundsByReason(reason);
        return ResponseEntity.ok(refunds);
    }
    
    // GET - Get refunds by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<RefundResponse>> getRefundsByTransactionId(@PathVariable String transactionId) {
        log.info("Retrieving refunds for transaction: {}", transactionId);
        
        List<RefundResponse> refunds = refundService.getRefundsByTransactionId(transactionId);
        return ResponseEntity.ok(refunds);
    }
    
    // POST - Update refund
    @PostMapping("/{id}/update")
    public ResponseEntity<RefundResponse> updateRefund(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        log.info("Updating refund with ID: {}", id);
        
        RefundResponse response = refundService.updateRefund(id, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // PUT - Update refund status
    @PutMapping("/{id}/status")
    public ResponseEntity<RefundResponse> updateRefundStatus(
            @PathVariable Long id, 
            @RequestParam Refund.RefundStatus status) {
        log.info("Updating refund status to {} for ID: {}", status, id);
        
        RefundResponse response = refundService.updateRefundStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Bank webhook for refund status updates
    @PostMapping("/webhooks/garanti")
    public ResponseEntity<String> handleGarantiRefundWebhook(@RequestBody String webhookData) {
        log.info("Received Garanti BBVA refund webhook: {}", webhookData);
        
        try {
            // Process Garanti BBVA refund webhook
            refundService.processBankRefundWebhook("GARANTI", webhookData);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing Garanti BBVA refund webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }
    
    @PostMapping("/webhooks/isbank")
    public ResponseEntity<String> handleIsBankRefundWebhook(@RequestBody String webhookData) {
        log.info("Received İş Bankası refund webhook: {}", webhookData);
        
        try {
            // Process İş Bankası refund webhook
            refundService.processBankRefundWebhook("ISBANK", webhookData);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing İş Bankası refund webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }
    
    @PostMapping("/webhooks/akbank")
    public ResponseEntity<String> handleAkbankRefundWebhook(@RequestBody String webhookData) {
        log.info("Received Akbank refund webhook: {}", webhookData);
        
        try {
            // Process Akbank refund webhook
            refundService.processBankRefundWebhook("AKBANK", webhookData);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing Akbank refund webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }
    
    // POST - Test endpoint to manually trigger refund scheduler (for testing purposes)
    @PostMapping("/test/trigger-scheduler")
    public ResponseEntity<String> triggerRefundScheduler() {
        log.info("Manually triggering refund scheduler for testing");
        
        try {
            // This will be handled by the scheduler component
            return ResponseEntity.ok("Refund scheduler triggered successfully. Check logs for details.");
        } catch (Exception e) {
            log.error("Error triggering refund scheduler: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to trigger scheduler: " + e.getMessage());
        }
    }
    
    // POST - Simulate bank webhook for refund status update
    @PostMapping("/{refundId}/simulate-bank-webhook")
    public ResponseEntity<RefundResponse> simulateBankWebhook(
            @PathVariable String refundId,
            @RequestBody SimulateWebhookRequest request) {
        log.info("Simulating bank webhook for refund: {} with status: {}", refundId, request.getStatus());
        
        try {
            RefundResponse response = refundService.simulateBankWebhook(refundId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error simulating bank webhook: {}", e.getMessage());
            RefundResponse errorResponse = new RefundResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Failed to simulate webhook: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
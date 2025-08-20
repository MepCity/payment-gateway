package com.payment.gateway.controller;

import com.payment.gateway.dto.DisputeRequest;
import com.payment.gateway.dto.DisputeResponse;
import com.payment.gateway.model.Dispute;
import com.payment.gateway.service.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/disputes")
@RequiredArgsConstructor
@Slf4j
public class DisputeController {
    
    private final DisputeService disputeService;
    
    // POST - Create new dispute
    @PostMapping
    public ResponseEntity<DisputeResponse> createDispute(@Valid @RequestBody DisputeRequest request) {
        log.info("Creating new dispute for payment: {}, transaction: {}", 
                request.getPaymentId(), request.getTransactionId());
        
        DisputeResponse response = disputeService.createDispute(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // GET - Get dispute by ID
    @GetMapping("/{id}")
    public ResponseEntity<DisputeResponse> getDisputeById(@PathVariable Long id) {
        log.info("Retrieving dispute with ID: {}", id);
        
        DisputeResponse response = disputeService.getDisputeById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get dispute by dispute ID
    @GetMapping("/dispute-id/{disputeId}")
    public ResponseEntity<DisputeResponse> getDisputeByDisputeId(@PathVariable String disputeId) {
        log.info("Retrieving dispute with dispute ID: {}", disputeId);
        
        DisputeResponse response = disputeService.getDisputeByDisputeId(disputeId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get dispute by payment ID
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<DisputeResponse> getDisputeByPaymentId(@PathVariable String paymentId) {
        log.info("Retrieving dispute with payment ID: {}", paymentId);
        
        DisputeResponse response = disputeService.getDisputeByPaymentId(paymentId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get all disputes
    @GetMapping
    public ResponseEntity<List<DisputeResponse>> getAllDisputes() {
        log.info("Retrieving all disputes");
        
        List<DisputeResponse> disputes = disputeService.getAllDisputes();
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by merchant ID
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByMerchantId(@PathVariable String merchantId) {
        log.info("Retrieving disputes for merchant: {}", merchantId);
        
        List<DisputeResponse> disputes = disputeService.getDisputesByMerchantId(merchantId);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByCustomerId(@PathVariable String customerId) {
        log.info("Retrieving disputes for customer: {}", customerId);
        
        List<DisputeResponse> disputes = disputeService.getDisputesByCustomerId(customerId);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByStatus(@PathVariable Dispute.DisputeStatus status) {
        log.info("Retrieving disputes with status: {}", status);
        
        List<DisputeResponse> disputes = disputeService.getDisputesByStatus(status);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by reason
    @GetMapping("/reason/{reason}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByReason(@PathVariable Dispute.DisputeReason reason) {
        log.info("Retrieving disputes with reason: {}", reason);
        
        List<DisputeResponse> disputes = disputeService.getDisputesByReason(reason);
        return ResponseEntity.ok(disputes);
    }
    
    // GET - Get disputes by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<DisputeResponse>> getDisputesByTransactionId(@PathVariable String transactionId) {
        log.info("Retrieving disputes for transaction: {}", transactionId);
        
        List<DisputeResponse> disputes = disputeService.getDisputesByTransactionId(transactionId);
        return ResponseEntity.ok(disputes);
    }
    
    // POST - Update dispute
    @PostMapping("/{id}/update")
    public ResponseEntity<DisputeResponse> updateDispute(@PathVariable Long id, @Valid @RequestBody DisputeRequest request) {
        log.info("Updating dispute with ID: {}", id);
        
        DisputeResponse response = disputeService.updateDispute(id, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Add evidence to dispute (Merchant kanıt gönderir)
    @PostMapping("/{id}/evidence")
    public ResponseEntity<DisputeResponse> addEvidenceToDispute(
            @PathVariable Long id,
            @RequestBody Map<String, String> evidenceRequest) {
        log.info("Adding evidence to dispute with ID: {}", id);
        
        String evidence = evidenceRequest.get("evidence");
        String additionalNotes = evidenceRequest.get("additionalNotes");
        
        DisputeResponse response = disputeService.addEvidenceToDispute(id, evidence, additionalNotes);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Evaluate dispute (Admin değerlendirir)
    @PostMapping("/{id}/evaluate")
    public ResponseEntity<DisputeResponse> evaluateDispute(
            @PathVariable Long id,
            @RequestBody Map<String, String> evaluationRequest) {
        log.info("Evaluating dispute with ID: {}", id);
        
        String decision = evaluationRequest.get("decision"); // APPROVED, REJECTED, PARTIAL_REFUND
        String adminNotes = evaluationRequest.get("adminNotes");
        String refundAmount = evaluationRequest.get("refundAmount"); // Eğer partial refund ise
        
        DisputeResponse response = disputeService.evaluateDispute(id, decision, adminNotes, refundAmount);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Notify merchant about dispute result
    @PostMapping("/{id}/notify-merchant")
    public ResponseEntity<DisputeResponse> notifyMerchantAboutDisputeResult(@PathVariable Long id) {
        log.info("Notifying merchant about dispute result for ID: {}", id);
        
        DisputeResponse response = disputeService.notifyMerchantAboutDisputeResult(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // PUT - Update dispute status
    @PutMapping("/{id}/status")
    public ResponseEntity<DisputeResponse> updateDisputeStatus(
            @PathVariable Long id, 
            @RequestParam Dispute.DisputeStatus status) {
        log.info("Updating dispute status to {} for ID: {}", status, id);
        
        DisputeResponse response = disputeService.updateDisputeStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Close dispute
    @PostMapping("/{id}/close")
    public ResponseEntity<DisputeResponse> closeDispute(@PathVariable Long id) {
        log.info("Closing dispute with ID: {}", id);
        
        DisputeResponse response = disputeService.closeDispute(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}

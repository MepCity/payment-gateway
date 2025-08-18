package com.payment.gateway.controller;

import com.payment.gateway.dto.RefundRequest;
import com.payment.gateway.dto.RefundResponse;
import com.payment.gateway.model.Refund;
import com.payment.gateway.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    

}

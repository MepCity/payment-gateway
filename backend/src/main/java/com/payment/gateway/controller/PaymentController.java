package com.payment.gateway.controller;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.PaymentResponse;
import com.payment.gateway.model.Payment;
import com.payment.gateway.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    // POST - Create new payment
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Creating new payment for merchant: {}, customer: {}", 
                request.getMerchantId(), request.getCustomerId());
        
        PaymentResponse response = paymentService.createPayment(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // GET - Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        log.info("Retrieving payment with ID: {}", id);
        
        PaymentResponse response = paymentService.getPaymentById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get payment by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<PaymentResponse> getPaymentByTransactionId(@PathVariable String transactionId) {
        log.info("Retrieving payment with transaction ID: {}", transactionId);
        
        PaymentResponse response = paymentService.getPaymentByTransactionId(transactionId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET - Get all payments
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        log.info("Retrieving all payments");
        
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    
    // GET - Get payments by merchant ID
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByMerchantId(@PathVariable String merchantId) {
        log.info("Retrieving payments for merchant: {}", merchantId);
        
        List<PaymentResponse> payments = paymentService.getPaymentsByMerchantId(merchantId);
        return ResponseEntity.ok(payments);
    }
    
    // GET - Get payments by customer ID
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomerId(@PathVariable String customerId) {
        log.info("Retrieving payments for customer: {}", customerId);
        
        List<PaymentResponse> payments = paymentService.getPaymentsByCustomerId(customerId);
        return ResponseEntity.ok(payments);
    }
    
    // GET - Get payments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable Payment.PaymentStatus status) {
        log.info("Retrieving payments with status: {}", status);
        
        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }
    
    // PUT - Update payment status
    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long id, 
            @RequestParam Payment.PaymentStatus status) {
        log.info("Updating payment status to {} for ID: {}", status, id);
        
        PaymentResponse response = paymentService.updatePaymentStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // DELETE - Delete payment
    @DeleteMapping("/{id}")
    public ResponseEntity<PaymentResponse> deletePayment(@PathVariable Long id) {
        log.info("Deleting payment with ID: {}", id);
        
        PaymentResponse response = paymentService.deletePayment(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // POST - Refund payment
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long id) {
        log.info("Refunding payment with ID: {}", id);
        
        PaymentResponse response = paymentService.refundPayment(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Gateway is running!");
    }
}

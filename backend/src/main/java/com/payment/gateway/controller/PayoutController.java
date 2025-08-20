package com.payment.gateway.controller;

import com.payment.gateway.dto.PayoutRequest;
import com.payment.gateway.dto.PayoutResponse;
import com.payment.gateway.model.Payout;
import com.payment.gateway.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/payouts")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PayoutController {
    
    private final PayoutService payoutService;
    
    @PostMapping("/")
    public ResponseEntity<PayoutResponse> createPayout(@Valid @RequestBody PayoutRequest request) {
        log.info("Received payout creation request for merchant: {}", request.getMerchantId());
        PayoutResponse response = payoutService.createPayout(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PayoutResponse> getPayoutById(@PathVariable Long id) {
        log.info("Retrieving payout by ID: {}", id);
        PayoutResponse response = payoutService.getPayoutById(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/payout-id/{payoutId}")
    public ResponseEntity<PayoutResponse> getPayoutByPayoutId(@PathVariable String payoutId) {
        log.info("Retrieving payout by payout ID: {}", payoutId);
        PayoutResponse response = payoutService.getPayoutByPayoutId(payoutId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/")
    public ResponseEntity<List<PayoutResponse>> getAllPayouts() {
        log.info("Retrieving all payouts");
        List<PayoutResponse> payouts = payoutService.getAllPayouts();
        return ResponseEntity.ok(payouts);
    }
    
    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<PayoutResponse>> getPayoutsByMerchantId(@PathVariable String merchantId) {
        log.info("Retrieving payouts for merchant: {}", merchantId);
        List<PayoutResponse> payouts = payoutService.getPayoutsByMerchantId(merchantId);
        return ResponseEntity.ok(payouts);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PayoutResponse>> getPayoutsByCustomerId(@PathVariable String customerId) {
        log.info("Retrieving payouts for customer: {}", customerId);
        List<PayoutResponse> payouts = payoutService.getPayoutsByCustomerId(customerId);
        return ResponseEntity.ok(payouts);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PayoutResponse>> getPayoutsByStatus(@PathVariable Payout.PayoutStatus status) {
        log.info("Retrieving payouts by status: {}", status);
        List<PayoutResponse> payouts = payoutService.getPayoutsByStatus(status);
        return ResponseEntity.ok(payouts);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<PayoutResponse>> getPayoutsByType(@PathVariable Payout.PayoutType type) {
        log.info("Retrieving payouts by type: {}", type);
        List<PayoutResponse> payouts = payoutService.getPayoutsByType(type);
        return ResponseEntity.ok(payouts);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<PayoutResponse> updatePayoutStatus(
            @PathVariable Long id,
            @RequestParam Payout.PayoutStatus status) {
        log.info("Updating payout status to {} for ID: {}", status, id);
        PayoutResponse response = payoutService.updatePayoutStatus(id, status);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<PayoutResponse> cancelPayout(@PathVariable Long id) {
        log.info("Cancelling payout with ID: {}", id);
        PayoutResponse response = payoutService.cancelPayout(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<PayoutResponse> deletePayout(@PathVariable Long id) {
        log.info("Deleting payout with ID: {}", id);
        PayoutResponse response = payoutService.deletePayout(id);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/merchant/{merchantId}/total")
    public ResponseEntity<BigDecimal> getTotalPayoutAmountByMerchant(@PathVariable String merchantId) {
        log.info("Getting total payout amount for merchant: {}", merchantId);
        BigDecimal total = payoutService.getTotalPayoutAmountByMerchant(merchantId);
        return ResponseEntity.ok(total);
    }
    
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> getPayoutCountByStatus(@PathVariable Payout.PayoutStatus status) {
        log.info("Getting payout count for status: {}", status);
        Long count = payoutService.getPayoutCountByStatus(status);
        return ResponseEntity.ok(count);
    }
}

package com.payment.gateway.controller;

import com.payment.gateway.dto.PaymentResponse;
import com.payment.gateway.dto.RefundResponse;
import com.payment.gateway.dto.MerchantResponse;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.RefundService;
import com.payment.gateway.service.MerchantService;
import com.payment.gateway.service.MerchantContextService;
import com.payment.gateway.model.Merchant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin kullanıcılar için controller
 * Tüm merchant verilerini görebilir
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final MerchantService merchantService;
    private final MerchantContextService merchantContextService;
    
    /**
     * Tüm merchant'ları listele
     */
    @GetMapping("/merchants")
    public ResponseEntity<List<MerchantResponse>> getAllMerchants() {
        if (!merchantContextService.isAdminUser()) {
            return ResponseEntity.status(403).build();
        }
        
        List<MerchantResponse> merchants = merchantService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }
    
    /**
     * Tüm ödemeleri listele (admin için)
     */
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        if (!merchantContextService.isAdminUser()) {
            return ResponseEntity.status(403).build();
        }
        
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Belirli merchant'ın ödemelerini listele
     */
    @GetMapping("/merchants/{merchantId}/payments")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByMerchant(@PathVariable String merchantId) {
        if (!merchantContextService.isAdminUser()) {
            return ResponseEntity.status(403).build();
        }
        
        List<PaymentResponse> payments = paymentService.getPaymentsByMerchantId(merchantId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Tüm refund'ları listele (admin için)
     */
    @GetMapping("/refunds")
    public ResponseEntity<List<RefundResponse>> getAllRefunds() {
        if (!merchantContextService.isAdminUser()) {
            return ResponseEntity.status(403).build();
        }
        
        List<RefundResponse> refunds = refundService.getAllRefunds();
        return ResponseEntity.ok(refunds);
    }
    
    /**
     * Belirli merchant'ın refund'larını listele
     */
    @GetMapping("/merchants/{merchantId}/refunds")
    public ResponseEntity<List<RefundResponse>> getRefundsByMerchant(@PathVariable String merchantId) {
        if (!merchantContextService.isAdminUser()) {
            return ResponseEntity.status(403).build();
        }
        
        List<RefundResponse> refunds = refundService.getRefundsByMerchantId(merchantId);
        return ResponseEntity.ok(refunds);
    }
    
    // Dispute methods moved to AdminDisputeController to avoid mapping conflicts
    
    /**
     * Dashboard istatistikleri (admin için)
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardStats> getDashboardStats() {
        if (!merchantContextService.isAdminUser()) {
            return ResponseEntity.status(403).build();
        }
        
        AdminDashboardStats stats = new AdminDashboardStats();
        stats.setTotalMerchants(merchantService.getMerchantCount());
        stats.setActiveMerchants(merchantService.getMerchantCountByStatus(Merchant.MerchantStatus.ACTIVE));
        
        // Bu kısımda gerçek istatistikler hesaplanabilir
        // Şimdilik basit değerler
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Admin dashboard istatistikleri için DTO
     */
    public static class AdminDashboardStats {
        private long totalMerchants;
        private long activeMerchants;
        
        // Getters and Setters
        public long getTotalMerchants() { return totalMerchants; }
        public void setTotalMerchants(long totalMerchants) { this.totalMerchants = totalMerchants; }
        
        public long getActiveMerchants() { return activeMerchants; }
        public void setActiveMerchants(long activeMerchants) { this.activeMerchants = activeMerchants; }
    }
}

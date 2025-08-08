package com.payment.gateway.controller;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.PaymentResponse;
import com.payment.gateway.model.Payment;
import com.payment.gateway.service.PaymentService;
import com.payment.gateway.service.MerchantAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final MerchantAuthService merchantAuthService;
    
    // POST - Create new payment
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        
        log.info("🔐 Payment request - Merchant: {}, API Key: {}", 
                request.getMerchantId(), apiKey != null ? "***" + apiKey.substring(Math.max(0, apiKey.length() - 4)) : "missing");
        
        // 1. API Key kontrolü
        if (!merchantAuthService.isValidApiKey(apiKey)) {
            log.warn("🚫 Geçersiz API key ile ödeme denemesi");
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Geçersiz API key. Lütfen doğru API key kullanın.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // 2. API Key ve Merchant ID eşleşmesi kontrolü
        if (!merchantAuthService.validateMerchantAccess(apiKey, request.getMerchantId())) {
            log.warn("🚫 API key ve merchant ID uyumsuzluğu - API: {}, Merchant: {}", 
                apiKey, request.getMerchantId());
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("API key bu merchant için geçerli değil.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        log.info("✅ Merchant authentication başarılı - Processing payment for: {}", 
                request.getMerchantId());
        
        // 3. Ödeme işlemini gerçekleştir
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
    
    // 3D Secure Success Callback
    @PostMapping("/3d-callback/success")
    public ResponseEntity<String> handle3DSecureSuccess(@RequestParam Map<String, String> params) {
        log.info("3D Secure success callback received with params: {}", params);
        
        try {
            String orderId = params.get("orderId");
            String transactionId = params.get("transactionId");
            String authCode = params.get("authCode");
            
            if (orderId != null) {
                // Payment'i başarılı olarak güncelle
                PaymentResponse response = paymentService.complete3DSecurePayment(orderId, transactionId, authCode, true);
                
                if (response.isSuccess()) {
                    // Başarılı ödeme sonrası yönlendirme sayfası
                    return ResponseEntity.ok("""
                        <html>
                        <head><title>Payment Successful</title></head>
                        <body>
                        <h2>✅ Payment Successful!</h2>
                        <p>Transaction ID: %s</p>
                        <p>Order ID: %s</p>
                        <script>
                            setTimeout(function() {
                                window.close();
                            }, 3000);
                        </script>
                        </body>
                        </html>
                        """.formatted(transactionId, orderId));
                } else {
                    return ResponseEntity.badRequest().body("Payment completion failed: " + response.getMessage());
                }
            } else {
                return ResponseEntity.badRequest().body("Missing orderId parameter");
            }
            
        } catch (Exception e) {
            log.error("Error processing 3D Secure success callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing 3D Secure callback: " + e.getMessage());
        }
    }
    
    // 3D Secure Fail Callback
    @PostMapping("/3d-callback/fail")
    public ResponseEntity<String> handle3DSecureFail(@RequestParam Map<String, String> params) {
        log.info("3D Secure fail callback received with params: {}", params);
        
        try {
            String orderId = params.get("orderId");
            String errorMessage = params.get("errorMessage");
            
            if (orderId != null) {
                // Payment'i başarısız olarak güncelle
                PaymentResponse response = paymentService.complete3DSecurePayment(orderId, null, null, false);
                
                // Başarısız ödeme sonrası yönlendirme sayfası
                return ResponseEntity.ok("""
                    <html>
                    <head><title>Payment Failed</title></head>
                    <body>
                    <h2>❌ Payment Failed!</h2>
                    <p>Order ID: %s</p>
                    <p>Error: %s</p>
                    <script>
                        setTimeout(function() {
                            window.close();
                        }, 3000);
                    </script>
                    </body>
                    </html>
                    """.formatted(orderId, errorMessage != null ? errorMessage : "3D Secure authentication failed"));
            } else {
                return ResponseEntity.badRequest().body("Missing orderId parameter");
            }
            
        } catch (Exception e) {
            log.error("Error processing 3D Secure fail callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing 3D Secure callback: " + e.getMessage());
        }
    }

    /**
     * Banka webhook endpoint'i - Garanti BBVA'dan gelen bildirimler
     */
    @PostMapping("/bank-webhooks/garanti")
    public ResponseEntity<Map<String, Object>> handleGarantiWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader Map<String, String> headers) {
        
        log.info("🏦 Garanti BBVA webhook alındı: {}", webhookData);
        log.info("📋 Headers: {}", headers);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String eventType = (String) webhookData.get("eventType");
            String orderId = (String) webhookData.get("orderId");
            String status = (String) webhookData.get("status");
            
            log.info("🔄 Event Type: {}, Order ID: {}, Status: {}", eventType, orderId, status);
            
            switch (eventType) {
                case "3D_SECURE_RESULT":
                    handle3DSecureResult(orderId, webhookData);
                    break;
                case "PAYMENT_STATUS_CHANGE":
                    handlePaymentStatusChange(orderId, status, webhookData);
                    break;
                case "CHARGEBACK":
                    handleChargeback(orderId, webhookData);
                    break;
                case "SETTLEMENT":
                    handleSettlement(orderId, webhookData);
                    break;
                default:
                    log.warn("⚠️ Bilinmeyen event type: {}", eventType);
            }
            
            response.put("status", "SUCCESS");
            response.put("message", "Webhook başarıyla işlendi");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Garanti webhook işlenirken hata", e);
            response.put("status", "ERROR");
            response.put("message", "Webhook işlenirken hata: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Diğer bankalar için webhook endpoint'leri
     */
    @PostMapping("/bank-webhooks/isbank")
    public ResponseEntity<Map<String, Object>> handleIsBankWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader Map<String, String> headers) {
        
        log.info("🏦 İş Bankası webhook alındı: {}", webhookData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "İş Bankası webhook işlendi");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/bank-webhooks/akbank")
    public ResponseEntity<Map<String, Object>> handleAkbankWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader Map<String, String> headers) {
        
        log.info("🏦 Akbank webhook alındı: {}", webhookData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Akbank webhook işlendi");
        
        return ResponseEntity.ok(response);
    }
    
    // Webhook helper methods
    private void handle3DSecureResult(String orderId, Map<String, Object> data) {
        log.info("🔐 3D Secure sonucu işleniyor - Order: {}", orderId);
        String status = (String) data.get("status");
        String authCode = (String) data.get("authCode");
        
        if ("SUCCESS".equals(status)) {
            log.info("✅ 3D Secure başarılı - Order: {}, AuthCode: {}", orderId, authCode);
            // Payment'ı başarılı olarak güncelle
            // paymentService.complete3DSecurePayment(orderId, authCode);
        } else {
            log.warn("❌ 3D Secure başarısız - Order: {}", orderId);
            // Payment'ı başarısız olarak güncelle
            // paymentService.fail3DSecurePayment(orderId, (String) data.get("errorMessage"));
        }
    }
    
    private void handlePaymentStatusChange(String orderId, String status, Map<String, Object> data) {
        log.info("💳 Ödeme durumu değişti - Order: {}, Yeni durum: {}", orderId, status);
    }
    
    private void handleChargeback(String orderId, Map<String, Object> data) {
        log.info("🔄 Chargeback bildirimi - Order: {}", orderId);
        String reason = (String) data.get("reason");
        String amount = (String) data.get("amount");
        log.info("📝 Chargeback nedeni: {}, Tutar: {}", reason, amount);
    }
    
    private void handleSettlement(String orderId, Map<String, Object> data) {
        log.info("💰 Settlement bildirimi - Order: {}", orderId);
        String settledAmount = (String) data.get("settledAmount");
        String settlementDate = (String) data.get("settlementDate");
        log.info("💵 Tahsilat tutarı: {}, Tarih: {}", settledAmount, settlementDate);
    }
    
    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Payment Gateway is running!");
    }
}

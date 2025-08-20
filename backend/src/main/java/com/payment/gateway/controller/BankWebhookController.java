package com.payment.gateway.controller;

import com.payment.gateway.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/bank-webhooks")
@RequiredArgsConstructor
@Slf4j
public class BankWebhookController {
    
    private final RefundService refundService;
    
    /**
     * Garanti BBVA'dan gelen webhook'lar
     */
    @PostMapping("/garanti")
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
     * İş Bankası'ndan gelen webhook'lar
     */
    @PostMapping("/isbank")
    public ResponseEntity<Map<String, Object>> handleIsBankWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader Map<String, String> headers) {
        
        log.info("🏦 İş Bankası webhook alındı: {}", webhookData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "İş Bankası webhook işlendi");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Akbank'tan gelen webhook'lar
     */
    @PostMapping("/akbank")
    public ResponseEntity<Map<String, Object>> handleAkbankWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader Map<String, String> headers) {
        
        log.info("🏦 Akbank webhook alındı: {}", webhookData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Akbank webhook işlendi");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Garanti BBVA'dan gelen refund webhook'ı
     * Test için Postman ile çağrılabilir
     */
    @PostMapping("/garanti/refund")
    public ResponseEntity<Map<String, String>> handleGarantiRefundWebhook(@RequestBody Map<String, String> webhookData) {
        try {
            log.info("Received Garanti BBVA refund webhook: {}", webhookData);
            
            String gatewayRefundId = webhookData.get("gatewayRefundId");
            String status = webhookData.get("status"); // SUCCESS, FAILED, CANCELLED
            String message = webhookData.get("message");
            
            if (gatewayRefundId == null || status == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: gatewayRefundId and status"));
            }
            
            // Webhook data formatı: "gatewayRefundId|status|message"
            String webhookDataString = String.format("%s|%s|%s", 
                gatewayRefundId, status, message != null ? message : "No message");
            
            refundService.processBankRefundWebhook("GARANTI", webhookDataString);
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "Webhook processed successfully"));
            
        } catch (Exception e) {
            log.error("Error processing Garanti refund webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to process webhook: " + e.getMessage()));
        }
    }
    
    /**
     * İş Bankası'ndan gelen refund webhook'ı
     */
    @PostMapping("/isbank/refund")
    public ResponseEntity<Map<String, String>> handleIsBankRefundWebhook(@RequestBody Map<String, String> webhookData) {
        try {
            log.info("Received İş Bankası refund webhook: {}", webhookData);
            
            String gatewayRefundId = webhookData.get("gatewayRefundId");
            String status = webhookData.get("status");
            String message = webhookData.get("message");
            
            if (gatewayRefundId == null || status == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: gatewayRefundId and status"));
            }
            
            String webhookDataString = String.format("%s|%s|%s", 
                gatewayRefundId, status, message != null ? message : "No message");
            
            refundService.processBankRefundWebhook("ISBANK", webhookDataString);
            
            return ResponseEntity.ok(Map.of("status", "success", "message", "Webhook processed successfully"));
            
        } catch (Exception e) {
            log.error("Error processing İş Bankası refund webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to process webhook: " + e.getMessage()));
        }
    }
    
    /**
     * Test için - herhangi bir refund'ın durumunu değiştirmek için genel endpoint
     */
    @PostMapping("/test/refund-status")
    public ResponseEntity<Map<String, String>> updateRefundStatusForTest(
            @RequestBody Map<String, String> requestData) {
        try {
            String gatewayRefundId = requestData.get("gatewayRefundId");
            String status = requestData.get("status"); // SUCCESS, FAILED, CANCELLED
            String bankType = requestData.get("bankType"); // GARANTI, ISBANK, AKBANK
            String message = requestData.get("message");
            
            if (gatewayRefundId == null || status == null || bankType == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: gatewayRefundId, status, bankType"));
            }
            
            String webhookDataString = String.format("%s|%s|%s", 
                gatewayRefundId, status, message != null ? message : "Test update");
            
            refundService.processBankRefundWebhook(bankType, webhookDataString);
            
            return ResponseEntity.ok(Map.of(
                "status", "success", 
                "message", "Refund status updated successfully",
                "gatewayRefundId", gatewayRefundId,
                "newStatus", status
            ));
            
        } catch (Exception e) {
            log.error("Error updating refund status for test: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update refund status: " + e.getMessage()));
        }
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
}

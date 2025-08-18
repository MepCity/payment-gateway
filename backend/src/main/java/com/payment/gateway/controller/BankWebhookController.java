package com.payment.gateway.controller;

import com.payment.gateway.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Bankalardan gelen webhook'ları işleyen controller
 */
@RestController
@RequestMapping("/api/v1/bank-webhooks")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BankWebhookController {

    private final PaymentService paymentService;

    /**
     * Garanti BBVA'dan gelen webhook'lar
     */
    @PostMapping("/garanti")
    public ResponseEntity<Map<String, Object>> handleGarantiWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader Map<String, String> headers) {
        
        log.info("Garanti webhook received: {}", webhookData);
        log.info("Headers: {}", headers);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String eventType = (String) webhookData.get("eventType");
            String orderId = (String) webhookData.get("orderId");
            String status = (String) webhookData.get("status");
            
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
                    log.warn("Unknown event type: {}", eventType);
            }
            
            response.put("status", "SUCCESS");
            response.put("message", "Webhook processed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing Garanti webhook", e);
            response.put("status", "ERROR");
            response.put("message", "Error processing webhook: " + e.getMessage());
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
        
        log.info("İş Bankası webhook received: {}", webhookData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "İş Bankası webhook processed");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Akbank'tan gelen webhook'lar
     */
    @PostMapping("/akbank")
    public ResponseEntity<Map<String, Object>> handleAkbankWebhook(
            @RequestBody Map<String, Object> webhookData,
            @RequestHeader Map<String, String> headers) {
        
        log.info("Akbank webhook received: {}", webhookData);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Akbank webhook processed");
        
        return ResponseEntity.ok(response);
    }
    
    private void handle3DSecureResult(String orderId, Map<String, Object> data) {
        log.info("Processing 3D Secure result for order: {}", orderId);
        String status = (String) data.get("status");
        String authCode = (String) data.get("authCode");
        
        if ("SUCCESS".equals(status)) {
            // Ödemeyi başarılı olarak güncelle
            log.info("3D Secure successful for order: {}, authCode: {}", orderId, authCode);
            // paymentService.complete3DSecurePayment(orderId, authCode);
        } else {
            // Ödemeyi başarısız olarak güncelle
            log.warn("3D Secure failed for order: {}", orderId);
            // paymentService.fail3DSecurePayment(orderId, (String) data.get("errorMessage"));
        }
    }
    
    private void handlePaymentStatusChange(String orderId, String status, Map<String, Object> data) {
        log.info("Payment status change for order: {}, new status: {}", orderId, status);
        // Payment entity'yi güncelle
    }
    
    private void handleChargeback(String orderId, Map<String, Object> data) {
        log.info("Chargeback received for order: {}", orderId);
        String reason = (String) data.get("reason");
        String amount = (String) data.get("amount");
        
        // Dispute entity oluştur
        // disputeService.createDispute(orderId, "CHARGEBACK", reason, amount);
    }
    
    private void handleSettlement(String orderId, Map<String, Object> data) {
        log.info("Settlement notification for order: {}", orderId);
        String settledAmount = (String) data.get("settledAmount");
        String settlementDate = (String) data.get("settlementDate");
        
        // Settlement bilgilerini kaydet
    }
}

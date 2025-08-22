package com.payment.gateway.controller;

import com.payment.gateway.service.RefundService;
import com.payment.gateway.service.DisputeService;
import com.payment.gateway.service.PayoutService;
import com.payment.gateway.service.MerchantService;
import com.payment.gateway.dto.BankDisputeNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/v1/bank-webhooks")
@RequiredArgsConstructor
@Slf4j
public class BankWebhookController {
    
    private final RefundService refundService;

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

    /**
     * Payment ID'ye göre merchant'a payout bildirimi gönder
     */
    private boolean notifyMerchantAboutPayout(String paymentId, String status, String message,
                                           String bankName, String settledAmount, String settlementDate) {
        try {
            // Payment ID'ye göre payout bilgilerini al
            var payoutInfo = payoutService.getPayoutByPaymentId(paymentId);
            if (payoutInfo == null) {
                log.warn("⚠️ Payout not found for payment ID: {}", paymentId);
                return false;
            }

            String merchantId = payoutInfo.getMerchantId();
            String customerId = payoutInfo.getCustomerId();
            String amount = payoutInfo.getAmount().toString();
            String currency = payoutInfo.getCurrency();

            // Merchant'ın webhook URL'ini al
            String merchantWebhookUrl = merchantService.getMerchantWebhookUrl(merchantId);

            if (merchantWebhookUrl == null || merchantWebhookUrl.isEmpty()) {
                log.warn("⚠️ Merchant webhook URL not found for merchant: {}", merchantId);
                return false;
            }

            // Merchant'a gönderilecek payload hazırla
            Map<String, Object> merchantPayload = new HashMap<>();
            merchantPayload.put("eventType", "PAYOUT_STATUS_UPDATE");
            merchantPayload.put("paymentId", paymentId);
            merchantPayload.put("payoutId", payoutInfo.getPayoutId());
            merchantPayload.put("merchantId", merchantId);
            merchantPayload.put("customerId", customerId);
            merchantPayload.put("amount", amount);
            merchantPayload.put("currency", currency);
            merchantPayload.put("status", status);
            merchantPayload.put("message", message);
            merchantPayload.put("bankName", bankName);
            merchantPayload.put("settledAmount", settledAmount);
            merchantPayload.put("settlementDate", settlementDate);
            merchantPayload.put("timestamp", LocalDateTime.now().toString());

            // Merchant'a POST request gönder
            boolean notificationSent = sendMerchantNotification(merchantWebhookUrl, merchantPayload);

            if (notificationSent) {
                log.info("✅ Merchant notification sent successfully - Merchant: {}, Payment: {}", merchantId, paymentId);
            } else {
                log.error("❌ Failed to send merchant notification - Merchant: {}, Payment: {}", merchantId, paymentId);
            }

            return notificationSent;

        } catch (Exception e) {
            log.error("❌ Error notifying merchant about payout", e);
            return false;
        }
    }

    /**
     * Merchant'a webhook bildirimi gönder
     */
    private boolean sendMerchantNotification(String webhookUrl, Map<String, Object> payload) {
        try {
            // RestTemplate ile POST request gönder
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);

            boolean success = response.getStatusCode().is2xxSuccessful();
            log.info("📤 Merchant notification response - Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());

            return success;

        } catch (Exception e) {
            log.error("❌ Error sending merchant notification to: {}", webhookUrl, e);
            return false;
        }
    }
}
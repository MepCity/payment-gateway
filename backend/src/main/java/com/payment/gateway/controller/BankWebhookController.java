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
    private final DisputeService disputeService;
    private final PayoutService payoutService;
    private final MerchantService merchantService;
    private final RestTemplate restTemplate;

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

    /**
     * Test için - payout webhook'ını test etmek için genel endpoint
     */
    @PostMapping("/test/payout-status")
    public ResponseEntity<Map<String, String>> updatePayoutStatusForTest(
            @RequestBody Map<String, String> requestData) {
        try {
            String paymentId = requestData.get("paymentId");
            String status = requestData.get("status"); // PENDING, PROCESSING, COMPLETED, FAILED
            String bankName = requestData.get("bankName");
            String message = requestData.get("message");
            String failureReason = requestData.get("failureReason");
            String settledAmount = requestData.get("settledAmount");
            String settlementDate = requestData.get("settlementDate");

            if (paymentId == null || status == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing required fields: paymentId and status"));
            }

            log.info("🧪 Test payout webhook - Payment ID: {}, Status: {}, Bank: {}", paymentId, status, bankName);

            // Payout durumunu güncelle
            boolean payoutUpdated = payoutService.updatePayoutStatusByPaymentId(paymentId, status, message, failureReason);

            if (payoutUpdated) {
                // Payment ID'ye göre merchant'ı bul ve bildirim gönder
                boolean merchantNotified = notifyMerchantAboutPayout(paymentId, status, message, bankName, settledAmount, settlementDate);

                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Payout status updated successfully",
                    "paymentId", paymentId,
                    "newStatus", status,
                    "payoutUpdated", String.valueOf(payoutUpdated),
                    "merchantNotified", String.valueOf(merchantNotified)
                ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to update payout status"));
            }

        } catch (Exception e) {
            log.error("Error updating payout status for test: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update payout status: " + e.getMessage()));
        }
    }

    /**
     * Banka'dan gelen dispute webhook'ı
     * Customer bankaya itiraz ettiğinde tetiklenir
     */
    @PostMapping("/disputes/bank-initiated")
    public ResponseEntity<Map<String, Object>> handleBankDisputeNotification(@RequestBody Map<String, Object> disputeData) {
        log.info("🚨 Bank dispute notification received: {}", disputeData);

        Map<String, Object> response = new HashMap<>();

        try {
            String bankDisputeId = (String) disputeData.get("bankDisputeId");
            String paymentId = (String) disputeData.get("paymentId");
            String merchantId = (String) disputeData.get("merchantId");
            Double disputeAmount = (Double) disputeData.get("disputeAmount");
            String disputeReason = (String) disputeData.get("disputeReason");
            String responseDeadline = (String) disputeData.get("responseDeadline");
            String currency = (String) disputeData.get("currency");
            String bankName = (String) disputeData.get("bankName");
            String customerInfo = (String) disputeData.get("customerInfo");

            if (bankDisputeId == null || paymentId == null || merchantId == null) {
                response.put("status", "ERROR");
                response.put("message", "Missing required fields");
                return ResponseEntity.badRequest().body(response);
            }

            // BankDisputeNotification oluştur
            BankDisputeNotification notification = new BankDisputeNotification();
            notification.setBankDisputeId(bankDisputeId);
            notification.setPaymentId(paymentId);
            notification.setMerchantId(merchantId);
            notification.setDisputeAmount(BigDecimal.valueOf(disputeAmount != null ? disputeAmount : 0.0));
            notification.setCurrency(currency != null ? currency : "TRY");
            notification.setDisputeReason(disputeReason);
            notification.setDisputeDate(LocalDateTime.now());

            // Response deadline parse et
            if (responseDeadline != null) {
                try {
                    notification.setResponseDeadline(LocalDateTime.parse(responseDeadline, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    // Default: 7 gün
                    notification.setResponseDeadline(LocalDateTime.now().plusDays(7));
                }
            } else {
                notification.setResponseDeadline(LocalDateTime.now().plusDays(7));
            }

            notification.setBankName(bankName);
            notification.setCustomerInfo(customerInfo);

            // DisputeService'e gönder - yeni dispute oluştur
            disputeService.createBankInitiatedDispute(notification);

            log.info("✅ Bank dispute processed successfully - Dispute ID: {}, Merchant: {}",
                    bankDisputeId, merchantId);

            response.put("status", "SUCCESS");
            response.put("message", "Dispute notification received and processed");
            response.put("bankDisputeId", bankDisputeId);
            response.put("merchantNotified", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error processing bank dispute notification", e);
            response.put("status", "ERROR");
            response.put("message", "Failed to process dispute: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Banka'dan gelen payout webhook'ı
     * Payout durumu değiştiğinde tetiklenir
     */
    @PostMapping("/payouts")
    public ResponseEntity<Map<String, Object>> handlePayoutWebhook(@RequestBody Map<String, Object> payoutData) {
        log.info("💰 Bank payout webhook alındı: {}", payoutData);

        Map<String, Object> response = new HashMap<>();

        try {
            String paymentId = (String) payoutData.get("paymentId");
            String payoutId = (String) payoutData.get("payoutId");
            String status = (String) payoutData.get("status");
            String bankName = (String) payoutData.get("bankName");
            String message = (String) payoutData.get("message");
            String failureReason = (String) payoutData.get("failureReason");
            String settledAmount = (String) payoutData.get("settledAmount");
            String settlementDate = (String) payoutData.get("settlementDate");

            if (paymentId == null || status == null) {
                response.put("status", "ERROR");
                response.put("message", "Missing required fields: paymentId and status");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("🔄 Payout webhook - Payment ID: {}, Status: {}, Bank: {}", paymentId, status, bankName);

            // Payout durumunu güncelle
            boolean payoutUpdated = payoutService.updatePayoutStatusByPaymentId(paymentId, status, message, failureReason);

            if (payoutUpdated) {
                // Payment ID'ye göre merchant'ı bul ve bildirim gönder
                boolean merchantNotified = notifyMerchantAboutPayout(paymentId, status, message, bankName, settledAmount, settlementDate);

                response.put("status", "SUCCESS");
                response.put("message", "Payout webhook processed successfully");
                response.put("paymentId", paymentId);
                response.put("payoutUpdated", payoutUpdated);
                response.put("merchantNotified", merchantNotified);

                log.info("✅ Payout webhook başarıyla işlendi - Payment ID: {}, Merchant notified: {}",
                        paymentId, merchantNotified);

            } else {
                response.put("status", "ERROR");
                response.put("message", "Failed to update payout status");
                return ResponseEntity.badRequest().body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error processing payout webhook", e);
            response.put("status", "ERROR");
            response.put("message", "Failed to process payout webhook: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
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
package com.payment.gateway.service;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerçek banka test ortamları ile entegrasyon servisi
 * Şu anda Garanti BBVA test ortamı için hazırlanmıştır
 */
@Service
@Slf4j
public class RealBankIntegrationService {

    @Value("${app.bank.garanti.test.enabled:false}")
    private boolean garantiTestEnabled;

    @Value("${app.bank.garanti.test.endpoint:}")
    private String garantiTestEndpoint;

    @Value("${app.bank.garanti.test.merchant-id:}")
    private String garantiMerchantId;

    @Value("${app.bank.garanti.test.terminal-id:}")
    private String garantiTerminalId;

    @Value("${app.bank.garanti.test.username:}")
    private String garantiUsername;

    @Value("${app.bank.garanti.test.password:}")
    private String garantiPassword;

    @Value("${app.bank.callback.base-url:http://localhost:8080}")
    private String callbackBaseUrl;

    private final RestTemplate restTemplate;

    public RealBankIntegrationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Gerçek banka ile ödeme işlemi başlatır
     * Eğer 3D Secure gerekiyorsa, 3D Secure URL'ini döner
     */
    public BankPaymentResult processPayment(PaymentRequest request, Payment payment) {
        log.info("Processing payment through real bank integration for payment: {}", payment.getPaymentId());

        // Garanti BBVA test ortamı kontrolü
        if (garantiTestEnabled && isGarantiBBVACard(request.getCardNumber())) {
            return processGarantiBBVAPayment(request, payment);
        }

        // Diğer bankalar için gelecekte eklenebilir
        // if (isBankAsiaTurkCard(request.getCardNumber())) { ... }
        // if (isZiraatBankCard(request.getCardNumber())) { ... }

        // Varsayılan: simülasyon moduna geri dön
        log.info("No real bank integration found, falling back to simulation");
        return null;
    }

    /**
     * Garanti BBVA test ortamı ile ödeme işlemi
     */
    private BankPaymentResult processGarantiBBVAPayment(PaymentRequest request, Payment payment) {
        log.info("Processing Garanti BBVA test payment for card: {}", maskCardNumber(request.getCardNumber()));

        try {
            // Garanti BBVA test API çağrısı için payload hazırla
            Map<String, Object> garantiPayload = buildGarantiPayload(request, payment);

            // API çağrısı yap
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + encodeCredentials(garantiUsername, garantiPassword));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(garantiPayload, headers);

            log.info("Calling Garanti BBVA test endpoint: {}", garantiTestEndpoint);
            ResponseEntity<Map> response = restTemplate.exchange(
                garantiTestEndpoint + "/api/v1/payments",
                HttpMethod.POST,
                entity,
                Map.class
            );

            return handleGarantiResponse(response, payment);

        } catch (Exception e) {
            log.error("Error processing Garanti BBVA payment", e);
            return BankPaymentResult.builder()
                .success(false)
                .errorMessage("Garanti BBVA API Error: " + e.getMessage())
                .status(Payment.PaymentStatus.FAILED)
                .build();
        }
    }

    /**
     * Garanti BBVA API payload'ı oluşturur
     */
    private Map<String, Object> buildGarantiPayload(PaymentRequest request, Payment payment) {
        Map<String, Object> payload = new HashMap<>();
        
        // Garanti BBVA API format
        payload.put("merchantId", garantiMerchantId);
        payload.put("terminalId", garantiTerminalId);
        payload.put("orderId", payment.getPaymentId());
        payload.put("amount", request.getAmount().multiply(new BigDecimal("100")).intValue()); // Kuruş cinsinden
        payload.put("currency", "949"); // TRY currency code
        payload.put("cardNumber", request.getCardNumber().replaceAll("\\s", ""));
        payload.put("cardExpiry", request.getExpiryDate().replace("/", ""));
        payload.put("cardHolderName", request.getCardHolderName());
        payload.put("cvv", request.getCvv()); // CVV'yi bankaya gönder ama DB'de sakla
        
        // 3D Secure callback URLs
        payload.put("successUrl", callbackBaseUrl + "/api/v1/payments/3d-callback/success");
        payload.put("failUrl", callbackBaseUrl + "/api/v1/payments/3d-callback/fail");
        
        // İsteğe bağlı alanlar
        payload.put("description", request.getDescription());
        payload.put("customerEmail", "test@example.com"); // Production'da gerçek email
        
        log.debug("Garanti payload prepared for payment: {}", payment.getPaymentId());
        return payload;
    }

    /**
     * Garanti BBVA API yanıtını işler
     */
    private BankPaymentResult handleGarantiResponse(ResponseEntity<Map> response, Payment payment) {
        Map<String, Object> responseBody = response.getBody();
        
        if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
            String resultCode = (String) responseBody.get("resultCode");
            String resultMessage = (String) responseBody.get("resultMessage");
            String threeDUrl = (String) responseBody.get("threeDUrl");
            
            if ("00".equals(resultCode)) {
                // Başarılı işlem
                return BankPaymentResult.builder()
                    .success(true)
                    .status(Payment.PaymentStatus.COMPLETED)
                    .bankTransactionId((String) responseBody.get("transactionId"))
                    .bankResponseMessage(resultMessage)
                    .completedAt(LocalDateTime.now())
                    .build();
                    
            } else if (threeDUrl != null && !threeDUrl.isEmpty()) {
                // 3D Secure gerekli
                return BankPaymentResult.builder()
                    .success(false)
                    .requires3DSecure(true)
                    .threeDSecureUrl(threeDUrl)
                    .status(Payment.PaymentStatus.PENDING)
                    .bankResponseMessage("3D Secure authentication required")
                    .build();
                    
            } else {
                // Hata
                return BankPaymentResult.builder()
                    .success(false)
                    .status(Payment.PaymentStatus.FAILED)
                    .errorMessage(resultMessage)
                    .bankResponseMessage(resultMessage)
                    .build();
            }
        } else {
            return BankPaymentResult.builder()
                .success(false)
                .status(Payment.PaymentStatus.FAILED)
                .errorMessage("Invalid response from bank")
                .build();
        }
    }

    /**
     * Garanti BBVA kartı kontrolü (BIN bazlı)
     */
    private boolean isGarantiBBVACard(String cardNumber) {
        if (cardNumber == null) return false;
        String cleanCard = cardNumber.replaceAll("\\s", "");
        
        // Garanti BBVA BIN'leri
        return cleanCard.startsWith("482494") ||  // Test kartları
               cleanCard.startsWith("540061") ||  // Garanti Bonus
               cleanCard.startsWith("454360") ||  // Garanti Miles&Smiles
               cleanCard.startsWith("518134");    // Garanti Flexi
    }

    /**
     * Kart numarasını maskeler
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 10) return "****";
        return cardNumber.substring(0, 6) + "******" + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Basic Auth credentials encode eder
     */
    private String encodeCredentials(String username, String password) {
        String credentials = username + ":" + password;
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * Banka ödeme sonucu DTO'su
     */
    public static class BankPaymentResult {
        private boolean success;
        private Payment.PaymentStatus status;
        private String bankTransactionId;
        private String bankResponseMessage;
        private String errorMessage;
        private boolean requires3DSecure;
        private String threeDSecureUrl;
        private LocalDateTime completedAt;

        // Builder pattern için
        public static BankPaymentResultBuilder builder() {
            return new BankPaymentResultBuilder();
        }

        // Getters
        public boolean isSuccess() { return success; }
        public Payment.PaymentStatus getStatus() { return status; }
        public String getBankTransactionId() { return bankTransactionId; }
        public String getBankResponseMessage() { return bankResponseMessage; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isRequires3DSecure() { return requires3DSecure; }
        public String getThreeDSecureUrl() { return threeDSecureUrl; }
        public LocalDateTime getCompletedAt() { return completedAt; }

        // Builder sınıfı
        public static class BankPaymentResultBuilder {
            private BankPaymentResult result = new BankPaymentResult();

            public BankPaymentResultBuilder success(boolean success) {
                result.success = success;
                return this;
            }

            public BankPaymentResultBuilder status(Payment.PaymentStatus status) {
                result.status = status;
                return this;
            }

            public BankPaymentResultBuilder bankTransactionId(String bankTransactionId) {
                result.bankTransactionId = bankTransactionId;
                return this;
            }

            public BankPaymentResultBuilder bankResponseMessage(String bankResponseMessage) {
                result.bankResponseMessage = bankResponseMessage;
                return this;
            }

            public BankPaymentResultBuilder errorMessage(String errorMessage) {
                result.errorMessage = errorMessage;
                return this;
            }

            public BankPaymentResultBuilder requires3DSecure(boolean requires3DSecure) {
                result.requires3DSecure = requires3DSecure;
                return this;
            }

            public BankPaymentResultBuilder threeDSecureUrl(String threeDSecureUrl) {
                result.threeDSecureUrl = threeDSecureUrl;
                return this;
            }

            public BankPaymentResultBuilder completedAt(LocalDateTime completedAt) {
                result.completedAt = completedAt;
                return this;
            }

            public BankPaymentResult build() {
                return result;
            }
        }
    }
}
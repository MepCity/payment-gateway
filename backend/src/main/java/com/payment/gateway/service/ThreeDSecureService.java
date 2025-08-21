package com.payment.gateway.service;

import com.payment.gateway.adapter.BankAdapter;
import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.ThreeDSecureRequest;
import com.payment.gateway.dto.ThreeDSecureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.payment.gateway.service.AuditService;
import com.payment.gateway.model.AuditLog;

import java.util.Optional;
import java.util.UUID;

/**
 * 3D Secure Service
 * Otomatik bank detection ve uygun adapter seçimi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThreeDSecureService {
    
    private final BankAdapterRegistry bankAdapterRegistry;
    private final AuditService auditService;
    
    @Value("${app.payment.gateway.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Otomatik 3D Secure başlatma
     * Kart numarasına göre uygun bankayı bulur ve o bankanın formatında istek atar
     */
    public ThreeDSecureResponse initiate3DSecure(PaymentRequest paymentRequest, String customerIp, String userAgent) {
        log.info("Initiating 3D Secure for payment - Card: {}", maskCardNumber(paymentRequest.getCardNumber()));
        
        try {
            // 1. Kart numarasına göre uygun bank adapter'ı bul
            Optional<BankAdapter> adapterOpt = bankAdapterRegistry.findAdapterByCardNumber(paymentRequest.getCardNumber());
            
            if (adapterOpt.isEmpty()) {
                log.warn("No suitable bank adapter found for card: {}", maskCardNumber(paymentRequest.getCardNumber()));
                return ThreeDSecureResponse.builder()
                        .success(false)
                        .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                        .message("Bu kart için 3D Secure desteği bulunmamaktadır")
                        .build();
            }
            
            BankAdapter adapter = adapterOpt.get();
            log.info("Using {} adapter with format: {} -> {}", 
                    adapter.getBankName(), 
                    adapter.getRequestFormat(), 
                    adapter.getResponseFormat());
            
            // 2. ThreeDSecureRequest oluştur
            ThreeDSecureRequest threeDRequest = buildThreeDSecureRequest(paymentRequest, customerIp, userAgent);
            
            // 3. Seçilen adapter ile 3D Secure başlat
            ThreeDSecureResponse response = adapter.initiate3DSecure(paymentRequest, threeDRequest);
            
            // Audit logging
            auditService.createEvent()
                .eventType("THREE_D_SECURE_INITIATED")
                .severity(AuditLog.Severity.MEDIUM)
                .actor("system")
                .action("INITIATE")
                .resourceType("PAYMENT")
                .resourceId("3DS-" + UUID.randomUUID().toString().substring(0, 8))
                .additionalData("bankName", adapter.getBankName())
                .additionalData("status", response.getStatus().name())
                .additionalData("success", response.isSuccess())
                .complianceTag("PCI_DSS")
                .log();
            
            log.info("3D Secure initiation result - Bank: {}, Status: {}, Success: {}", 
                    adapter.getBankName(), response.getStatus(), response.isSuccess());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error during 3D Secure initiation: {}", e.getMessage(), e);
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("3D Secure başlatma sırasında hata oluştu: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Banka callback'ini işle
     */
    public ThreeDSecureResponse handle3DCallback(String bankName, String callbackData) {
        log.info("Handling 3D Secure callback from bank: {}", bankName);
        
        try {
            // Banka adına göre adapter bul
            Optional<BankAdapter> adapterOpt = bankAdapterRegistry.findAdapterByBankName(bankName);
            
            if (adapterOpt.isEmpty()) {
                log.error("No adapter found for bank: {}", bankName);
                return ThreeDSecureResponse.builder()
                        .success(false)
                        .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                        .message("Bilinmeyen banka callback'i: " + bankName)
                        .build();
            }
            
            BankAdapter adapter = adapterOpt.get();
            
            // Adapter ile callback'i işle
            ThreeDSecureResponse response = adapter.handle3DCallback(callbackData);
            
            log.info("3D Secure callback handled - Bank: {}, Status: {}, Success: {}", 
                    bankName, response.getStatus(), response.isSuccess());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error handling 3D Secure callback from {}: {}", bankName, e.getMessage(), e);
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Callback işleme hatası: " + e.getMessage())
                    .bankName(bankName)
                    .build();
        }
    }
    
    /**
     * Kart için 3D Secure desteği var mı kontrol et
     */
    public boolean is3DSecureSupported(String cardNumber) {
        return bankAdapterRegistry.findAdapterByCardNumber(cardNumber).isPresent();
    }
    
    /**
     * Kart hangi bankaya ait
     */
    public String detectBankName(String cardNumber) {
        Optional<BankAdapter> adapter = bankAdapterRegistry.findAdapterByCardNumber(cardNumber);
        return adapter.map(BankAdapter::getBankName).orElse("UNKNOWN");
    }
    
    /**
     * Kart için hangi format kullanılacak
     */
    public BankAdapter.RequestFormat detectRequestFormat(String cardNumber) {
        Optional<BankAdapter> adapter = bankAdapterRegistry.findAdapterByCardNumber(cardNumber);
        return adapter.map(BankAdapter::getRequestFormat).orElse(null);
    }
    
    /**
     * ThreeDSecureRequest builder
     */
    private ThreeDSecureRequest buildThreeDSecureRequest(PaymentRequest paymentRequest, String customerIp, String userAgent) {
        String orderId = generateOrderId();
        
        return ThreeDSecureRequest.builder()
                .orderId(orderId)
                .merchantId(paymentRequest.getMerchantId())
                .customerId(paymentRequest.getCustomerId())
                // Card Information
                .cardNumber(paymentRequest.getCardNumber())
                .cardHolderName(paymentRequest.getCardHolderName())
                .expiryMonth(paymentRequest.getExpiryDate().substring(0, 2))
                .expiryYear(paymentRequest.getExpiryDate().substring(3, 5))
                .cvv(paymentRequest.getCvv())
                // Transaction Information
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .description(paymentRequest.getDescription())
                // 3D Secure URLs
                .successUrl(baseUrl + "/api/v1/3dsecure/success")
                .failUrl(baseUrl + "/api/v1/3dsecure/fail")
                .callbackUrl(baseUrl + "/api/v1/3dsecure/callback/" + detectBankName(paymentRequest.getCardNumber()).toLowerCase())
                // Customer Information
                .customerIp(customerIp)
                .customerEmail("customer@example.com") // PaymentRequest'e eklenebilir
                .customerPhone("") // PaymentRequest'e eklenebilir
                // Browser Information
                .userAgent(userAgent)
                .acceptHeader("text/html,application/xhtml+xml")
                .language("tr-TR")
                .colorDepth("24")
                .screenHeight("1080")
                .screenWidth("1920")
                .timeZone("180") // GMT+3
                .javaEnabled("false")
                .javascriptEnabled("true")
                .build();
    }
    
    /**
     * Unique order ID oluştur
     */
    private String generateOrderId() {
        return "3DS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Kart numarasını maskele
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        return cardNumber.substring(0, 4) + "****" + cardNumber.substring(cardNumber.length() - 4);
    }
    
    /**
     * 3D Secure adapter istatistikleri
     */
    public Object getAdapterStats() {
        return bankAdapterRegistry.getAdapterStats();
    }
    
    /**
     * Adapter sağlık kontrolü
     */
    public Object performHealthCheck() {
        return bankAdapterRegistry.performHealthCheck();
    }
}
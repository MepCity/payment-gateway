package com.payment.gateway.adapter.impl;

import com.payment.gateway.adapter.AbstractBankAdapter;
import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.ThreeDSecureRequest;
import com.payment.gateway.dto.ThreeDSecureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * İş Bankası 3D Secure Adapter
 * İş Bankası JSON formatını kullanır (modern REST API)
 */
@Component
@Slf4j
public class IsBankAdapter extends AbstractBankAdapter {
    
    @Value("${app.bank.isbank.3d.enabled:true}")
    private boolean enabled;
    
    @Value("${app.bank.isbank.3d.endpoint:https://spos.isbank.com.tr/servlet/est3Dgate}")
    private String threeDEndpoint;
    
    @Value("${app.bank.isbank.test.client-id:700655000200}")
    private String clientId;
    
    @Value("${app.bank.isbank.test.username:ISTEST}")
    private String username;
    
    @Value("${app.bank.isbank.test.password:ISTEST123}")
    private String password;
    
    // İş Bankası BIN'leri
    private static final String[] ISBANK_BINS = {
        "454672", // İş Bankası Maximum
        "479184", // İş Bankası Axess
        "528207", // İş Bankası WorldCard
        "510043", // İş Bankası MasterCard
        "402360", // İş Bankası Visa
        "451963"  // İş Bankası Test kartları
    };
    
    @Override
    public String getBankName() {
        return "ISBANK";
    }
    
    @Override
    public RequestFormat getRequestFormat() {
        return RequestFormat.JSON;
    }
    
    @Override
    public ResponseFormat getResponseFormat() {
        return ResponseFormat.JSON;
    }
    
    @Override
    public boolean supportsBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return false;
        }
        
        String bin = cardNumber.substring(0, 6);
        for (String isBankBin : ISBANK_BINS) {
            if (bin.startsWith(isBankBin)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ThreeDSecureResponse initiate3DSecure(PaymentRequest paymentRequest, ThreeDSecureRequest threeDRequest) {
        log.info("Initiating 3D Secure with İş Bankası for order: {}", threeDRequest.getOrderId());
        
        try {
            // İş Bankası JSON request oluştur
            Map<String, Object> jsonRequest = buildIsBankJsonRequest(threeDRequest);
            
            // HTTP headers oluştur (Basic Auth)
            HttpHeaders headers = createBasicAuthHeaders(username, password);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            
            // İş Bankası'na JSON request gönder
            ResponseEntity<String> response = sendJsonRequest(threeDEndpoint, jsonRequest, headers);
            
            // Response'u parse et
            return parseIsBankResponse(response.getBody(), threeDRequest);
            
        } catch (Exception e) {
            log.error("Error initiating 3D Secure with İş Bankası: {}", e.getMessage());
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("İş Bankası 3D Secure başlatma hatası: " + e.getMessage())
                    .bankName(getBankName())
                    .build();
        }
    }
    
    @Override
    public ThreeDSecureResponse handle3DCallback(String callbackData) {
        log.info("Handling 3D Secure callback from İş Bankası");
        
        try {
            // İş Bankası JSON callback parse et
            Map<String, Object> callbackJson = parseJsonResponse(callbackData, Map.class);
            
            String resultCode = (String) callbackJson.get("resultCode");
            String orderId = (String) callbackJson.get("orderId");
            
            ThreeDSecureResponse.ThreeDStatus status;
            boolean success = false;
            String message = "";
            
            // İş Bankası result kodları
            switch (resultCode) {
                case "00":
                    status = ThreeDSecureResponse.ThreeDStatus.AUTHENTICATED;
                    success = true;
                    message = "3D Secure authentication successful";
                    break;
                case "05":
                    status = ThreeDSecureResponse.ThreeDStatus.NOT_ENROLLED;
                    message = "Card not enrolled for 3D Secure";
                    break;
                case "98":
                    status = ThreeDSecureResponse.ThreeDStatus.TIMEOUT;
                    message = "Transaction timeout";
                    break;
                case "99":
                    status = ThreeDSecureResponse.ThreeDStatus.ERROR;
                    message = "System error";
                    break;
                default:
                    status = ThreeDSecureResponse.ThreeDStatus.FAILED;
                    message = "Authentication failed: " + resultCode;
            }
            
            return ThreeDSecureResponse.builder()
                    .success(success)
                    .status(status)
                    .message(message)
                    .orderId(orderId)
                    .bankResponseCode(resultCode)
                    .bankResponseMessage((String) callbackJson.get("resultMessage"))
                    .cavv((String) callbackJson.get("cavv"))
                    .eci((String) callbackJson.get("eci"))
                    .xid((String) callbackJson.get("xid"))
                    .authCode((String) callbackJson.get("authCode"))
                    .bankName(getBankName())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error handling İş Bankası 3D callback: {}", e.getMessage());
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Callback işleme hatası: " + e.getMessage())
                    .bankName(getBankName())
                    .build();
        }
    }
    
    @Override
    public boolean isTestMode() {
        return true; // Test ortamı
    }
    
    @Override
    public boolean isConfigured() {
        return enabled && clientId != null && username != null && password != null;
    }
    
    /**
     * İş Bankası JSON request oluştur
     */
    private Map<String, Object> buildIsBankJsonRequest(ThreeDSecureRequest request) {
        Map<String, Object> json = new HashMap<>();
        
        // Merchant bilgileri
        json.put("clientId", clientId);
        json.put("amount", request.getAmount().multiply(new java.math.BigDecimal(100)).intValue()); // Kuruş cinsinden
        json.put("currency", "949"); // TRY
        json.put("orderId", request.getOrderId());
        
        // Kart bilgileri
        Map<String, Object> card = new HashMap<>();
        card.put("cardNumber", request.getCardNumber());
        card.put("expiryMonth", request.getExpiryMonth());
        card.put("expiryYear", request.getExpiryYear());
        card.put("cvv", request.getCvv());
        card.put("cardHolderName", request.getCardHolderName());
        json.put("card", card);
        
        // 3D Secure bilgileri
        Map<String, Object> threeDSecure = new HashMap<>();
        threeDSecure.put("successUrl", request.getSuccessUrl());
        threeDSecure.put("failUrl", request.getFailUrl());
        threeDSecure.put("callbackUrl", request.getCallbackUrl());
        json.put("threeDSecure", threeDSecure);
        
        // Müşteri bilgileri
        Map<String, Object> customer = new HashMap<>();
        customer.put("ipAddress", request.getCustomerIp());
        customer.put("email", request.getCustomerEmail());
        customer.put("phone", request.getCustomerPhone());
        json.put("customer", customer);
        
        // Browser bilgileri (3D Secure v2)
        if (request.getUserAgent() != null) {
            Map<String, Object> browser = new HashMap<>();
            browser.put("userAgent", request.getUserAgent());
            browser.put("acceptHeader", request.getAcceptHeader());
            browser.put("language", request.getLanguage());
            browser.put("colorDepth", request.getColorDepth());
            browser.put("screenHeight", request.getScreenHeight());
            browser.put("screenWidth", request.getScreenWidth());
            browser.put("timeZone", request.getTimeZone());
            browser.put("javaEnabled", request.getJavaEnabled());
            browser.put("javascriptEnabled", request.getJavascriptEnabled());
            json.put("browser", browser);
        }
        
        // Taksit bilgileri
        if (request.getInstallment() != null && !request.getInstallment().equals("0")) {
            json.put("installment", Integer.parseInt(request.getInstallment()));
        }
        
        return json;
    }
    
    /**
     * İş Bankası response parse et
     */
    private ThreeDSecureResponse parseIsBankResponse(String jsonResponse, ThreeDSecureRequest request) {
        try {
            Map<String, Object> response = parseJsonResponse(jsonResponse, Map.class);
            
            String resultCode = (String) response.get("resultCode");
            String resultMessage = (String) response.get("resultMessage");
            
            if ("00".equals(resultCode)) {
                // 3D Secure başlatıldı
                String redirectUrl = (String) response.get("threeDSecureUrl");
                String formData = (String) response.get("formData");
                
                return ThreeDSecureResponse.builder()
                        .success(true)
                        .status(ThreeDSecureResponse.ThreeDStatus.INITIATED)
                        .message("3D Secure başlatıldı, kullanıcı yönlendiriliyor")
                        .redirectUrl(redirectUrl)
                        .formData(formData)
                        .orderId(request.getOrderId())
                        .bankResponseCode(resultCode)
                        .bankResponseMessage(resultMessage)
                        .bankName(getBankName())
                        .build();
            } else {
                return ThreeDSecureResponse.builder()
                        .success(false)
                        .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                        .message("İş Bankası 3D Secure başlatma başarısız: " + resultMessage)
                        .errorCode(resultCode)
                        .errorMessage(resultMessage)
                        .bankName(getBankName())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("Error parsing İş Bankası response: {}", e.getMessage());
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Response parse hatası: " + e.getMessage())
                    .bankName(getBankName())
                    .build();
        }
    }
}

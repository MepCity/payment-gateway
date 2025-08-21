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
 * Yapı Kredi 3D Secure Adapter
 * Yapı Kredi Form Data formatını kullanır (application/x-www-form-urlencoded)
 */
@Component
@Slf4j
public class YapiKrediBankAdapter extends AbstractBankAdapter {
    
    @Value("${app.bank.yapikredi.3d.enabled:true}")
    private boolean enabled;
    
    @Value("${app.bank.yapikredi.3d.endpoint:https://setmpos.ykb.com/3DSWebService/YKBPaymentService}")
    private String threeDEndpoint;
    
    @Value("${app.bank.yapikredi.test.merchant-id:6706598320}")
    private String merchantId;
    
    @Value("${app.bank.yapikredi.test.terminal-id:67005551}")
    private String terminalId;
    
    @Value("${app.bank.yapikredi.test.posnet-id:27426}")
    private String posnetId;
    
    @Value("${app.bank.yapikredi.test.encryption-key:10,10,10,10,10,10,10,10}")
    private String encryptionKey;
    
    // Yapı Kredi BIN'leri
    private static final String[] YAPIKREDI_BINS = {
        "540667", // Yapı Kredi WorldCard
        "549273", // Yapı Kredi MasterCard
        "402671", // Yapı Kredi Visa
        "418709", // Yapı Kredi Bonus
        "520093", // Yapı Kredi Advantage
        "676371"  // Yapı Kredi Test kartları
    };
    
    @Override
    public String getBankName() {
        return "YAPIKREDI";
    }
    
    @Override
    public RequestFormat getRequestFormat() {
        return RequestFormat.FORM_DATA;
    }
    
    @Override
    public ResponseFormat getResponseFormat() {
        return ResponseFormat.FORM_POST;
    }
    
    @Override
    public boolean supportsBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return false;
        }
        
        String bin = cardNumber.substring(0, 6);
        for (String yapiKrediBin : YAPIKREDI_BINS) {
            if (bin.startsWith(yapiKrediBin)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ThreeDSecureResponse initiate3DSecure(PaymentRequest paymentRequest, ThreeDSecureRequest threeDRequest) {
        log.info("Initiating 3D Secure with Yapı Kredi for order: {}", threeDRequest.getOrderId());
        
        try {
            // Yapı Kredi form data oluştur
            Map<String, String> formData = buildYapiKrediFormData(threeDRequest);
            
            // HTTP headers oluştur
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");
            headers.set("User-Agent", "PaymentGateway/1.0");
            
            // Yapı Kredi'ye form data request gönder
            ResponseEntity<String> response = sendFormDataRequest(threeDEndpoint, formData, headers);
            
            // Response'u parse et
            return parseYapiKrediResponse(response.getBody(), threeDRequest);
            
        } catch (Exception e) {
            log.error("Error initiating 3D Secure with Yapı Kredi: {}", e.getMessage());
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Yapı Kredi 3D Secure başlatma hatası: " + e.getMessage())
                    .bankName(getBankName())
                    .build();
        }
    }
    
    @Override
    public ThreeDSecureResponse handle3DCallback(String callbackData) {
        log.info("Handling 3D Secure callback from Yapı Kredi");
        
        try {
            // Yapı Kredi callback form data parse et
            Map<String, String> callbackParams = parseFormData(callbackData);
            
            String approved = callbackParams.get("approved");
            String respCode = callbackParams.get("respCode");
            String orderId = callbackParams.get("orderId");
            
            ThreeDSecureResponse.ThreeDStatus status;
            boolean success = false;
            String message = "";
            
            // Yapı Kredi response kodları
            if ("1".equals(approved)) {
                status = ThreeDSecureResponse.ThreeDStatus.AUTHENTICATED;
                success = true;
                message = "3D Secure authentication successful";
            } else {
                switch (respCode) {
                    case "0001":
                        status = ThreeDSecureResponse.ThreeDStatus.NOT_ENROLLED;
                        message = "Card not enrolled for 3D Secure";
                        break;
                    case "0002":
                        status = ThreeDSecureResponse.ThreeDStatus.TIMEOUT;
                        message = "Transaction timeout";
                        break;
                    case "0003":
                        status = ThreeDSecureResponse.ThreeDStatus.ERROR;
                        message = "System error";
                        break;
                    case "0004":
                        status = ThreeDSecureResponse.ThreeDStatus.CANCELLED;
                        message = "Transaction cancelled by user";
                        break;
                    default:
                        status = ThreeDSecureResponse.ThreeDStatus.FAILED;
                        message = "Authentication failed: " + respCode;
                }
            }
            
            return ThreeDSecureResponse.builder()
                    .success(success)
                    .status(status)
                    .message(message)
                    .orderId(orderId)
                    .bankResponseCode(respCode)
                    .bankResponseMessage(callbackParams.get("respText"))
                    .cavv(callbackParams.get("cavv"))
                    .eci(callbackParams.get("eci"))
                    .xid(callbackParams.get("xid"))
                    .authCode(callbackParams.get("authCode"))
                    .hostReferenceNumber(callbackParams.get("hostRefNum"))
                    .bankName(getBankName())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error handling Yapı Kredi 3D callback: {}", e.getMessage());
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
        return enabled && merchantId != null && terminalId != null && posnetId != null && encryptionKey != null;
    }
    
    /**
     * Yapı Kredi form data oluştur
     */
    private Map<String, String> buildYapiKrediFormData(ThreeDSecureRequest request) {
        Map<String, String> formData = new HashMap<>();
        
        // Merchant bilgileri
        formData.put("mid", merchantId);
        formData.put("tid", terminalId);
        formData.put("posnetid", posnetId);
        
        // İşlem bilgileri
        formData.put("amount", request.getAmount().multiply(new java.math.BigDecimal(100)).toString()); // Kuruş cinsinden
        formData.put("currencycode", "949"); // TRY
        formData.put("orderid", request.getOrderId());
        formData.put("installment", request.getInstallment() != null ? request.getInstallment() : "00");
        
        // Kart bilgileri (encrypted)
        String encryptedData = encryptCardData(request);
        formData.put("data1", encryptedData);
        formData.put("data2", ""); // Ek veri
        
        // 3D Secure URLs
        formData.put("successURL", request.getSuccessUrl());
        formData.put("failURL", request.getFailUrl());
        
        // Müşteri bilgileri
        formData.put("customerIPAddress", request.getCustomerIp());
        formData.put("email", request.getCustomerEmail());
        
        // İmza (MAC)
        String mac = generateYapiKrediMac(formData);
        formData.put("mac", mac);
        
        // İşlem tipi
        formData.put("trantype", "Sale");
        formData.put("lang", "tr");
        
        return formData;
    }
    
    /**
     * Kart verilerini şifrele (Yapı Kredi formatı)
     */
    private String encryptCardData(ThreeDSecureRequest request) {
        // Basitleştirilmiş encryption (gerçek implementasyonda DES/3DES kullanılır)
        String cardData = request.getCardNumber() + ";" + 
                         request.getExpiryMonth() + request.getExpiryYear() + ";" +
                         request.getCvv() + ";" +
                         request.getCardHolderName();
        
        // Base64 encode (gerçek projede proper encryption yapılmalı)
        return java.util.Base64.getEncoder().encodeToString(cardData.getBytes());
    }
    
    /**
     * Yapı Kredi MAC oluştur
     */
    private String generateYapiKrediMac(Map<String, String> formData) {
        // MAC = SHA-1(mid + tid + amount + currencycode + orderid + installment + data1 + data2 + successURL + failURL + encryptionKey)
        String macData = formData.get("mid") + formData.get("tid") + 
                        formData.get("amount") + formData.get("currencycode") + 
                        formData.get("orderid") + formData.get("installment") + 
                        formData.get("data1") + formData.get("data2") + 
                        formData.get("successURL") + formData.get("failURL") + 
                        encryptionKey;
        
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(macData.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            log.error("Error generating Yapı Kredi MAC: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Yapı Kredi response parse et
     */
    private ThreeDSecureResponse parseYapiKrediResponse(String htmlResponse, ThreeDSecureRequest request) {
        // HTML response'dan 3D Secure form bilgilerini çıkar
        if (htmlResponse.contains("3DSecure") || htmlResponse.contains("form")) {
            // Form action URL'ini bul
            String formAction = extractFormAction(htmlResponse);
            
            // Auto-submit form oluştur
            Map<String, String> hiddenFields = extractHiddenFields(htmlResponse);
            String autoSubmitForm = createAutoSubmitForm(formAction, hiddenFields);
            
            return ThreeDSecureResponse.builder()
                    .success(true)
                    .status(ThreeDSecureResponse.ThreeDStatus.INITIATED)
                    .message("3D Secure başlatıldı, kullanıcı yönlendiriliyor")
                    .formData(autoSubmitForm)
                    .formAction(formAction)
                    .orderId(request.getOrderId())
                    .bankName(getBankName())
                    .build();
        } else {
            return ThreeDSecureResponse.builder()
                    .success(false)
                    .status(ThreeDSecureResponse.ThreeDStatus.ERROR)
                    .message("Yapı Kredi 3D Secure başlatma başarısız")
                    .bankName(getBankName())
                    .build();
        }
    }
    
    /**
     * Form data parse et
     */
    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    params.put(key, value);
                } catch (Exception e) {
                    log.warn("Error decoding form parameter: {}", pair);
                }
            }
        }
        
        return params;
    }
    
    /**
     * HTML'den form action URL'ini çıkar
     */
    private String extractFormAction(String html) {
        // Regex ile form action'ı bul
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("action=[\"'](.*?)[\"']");
        java.util.regex.Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    /**
     * HTML'den hidden field'ları çıkar
     */
    private Map<String, String> extractHiddenFields(String html) {
        Map<String, String> fields = new HashMap<>();
        
        // Hidden input field'ları bul
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "<input[^>]*type=[\"']hidden[\"'][^>]*name=[\"'](.*?)[\"'][^>]*value=[\"'](.*?)[\"'][^>]*>");
        java.util.regex.Matcher matcher = pattern.matcher(html);
        
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            fields.put(name, value);
        }
        
        return fields;
    }
}
package com.payment.gateway.adapter.impl;

import com.payment.gateway.adapter.AbstractBankAdapter;
import com.payment.gateway.dto.PaymentRequest;
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
    public boolean isTestMode() {
        return true; // Test ortamı
    }
    
    @Override
    public boolean isConfigured() {
        return enabled && merchantId != null && terminalId != null && posnetId != null && encryptionKey != null;
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

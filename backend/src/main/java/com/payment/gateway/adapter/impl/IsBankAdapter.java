package com.payment.gateway.adapter.impl;

import com.payment.gateway.adapter.AbstractBankAdapter;
import com.payment.gateway.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * İş Bankası Adapter
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
    public boolean isTestMode() {
        return true; // Test ortamı
    }
    
    @Override
    public boolean isConfigured() {
        return enabled && clientId != null && username != null && password != null;
    }
}

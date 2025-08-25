package com.payment.gateway.adapter.impl;

import com.payment.gateway.adapter.AbstractBankAdapter;
import com.payment.gateway.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Garanti BBVA Adapter
 * Garanti BBVA XML formatını kullanır
 */
@Component
@Slf4j
public class GarantiBankAdapter extends AbstractBankAdapter {
    
    @Value("${app.bank.garanti.3d.enabled:true}")
    private boolean enabled;
    
    @Value("${app.bank.garanti.3d.endpoint:https://sanalposprov.garanti.com.tr/VPosService/v3/Vpos}")
    private String threeDEndpoint;
    
    @Value("${app.bank.garanti.test.merchant-id:000000000111111}")
    private String merchantId;
    
    @Value("${app.bank.garanti.test.terminal-id:00111111}")
    private String terminalId;
    
    @Value("${app.bank.garanti.test.username:PROVAUT}")
    private String username;
    
    @Value("${app.bank.garanti.test.password:123456}")
    private String password;
    
    // Garanti BBVA BIN'leri
    private static final String[] GARANTI_BINS = {
        "482494", // Test kartları
        "540061", // Garanti Bonus
        "454360", // Garanti Miles&Smiles
        "518134", // Garanti Flexi
        "526717", // Garanti Maximum
        "554960"  // Garanti Advantage
    };
    
    @Override
    public String getBankName() {
        return "GARANTI_BBVA";
    }
    
    @Override
    public RequestFormat getRequestFormat() {
        return RequestFormat.XML;
    }
    
    @Override
    public ResponseFormat getResponseFormat() {
        return ResponseFormat.XML;
    }
    
    @Override
    public boolean supportsBin(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 6) {
            return false;
        }
        
        String bin = cardNumber.substring(0, 6);
        for (String garantiBin : GARANTI_BINS) {
            if (bin.startsWith(garantiBin)) {
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
        return enabled && merchantId != null && terminalId != null && username != null && password != null;
    }
}

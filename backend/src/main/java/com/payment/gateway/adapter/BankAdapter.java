package com.payment.gateway.adapter;

import com.payment.gateway.dto.PaymentRequest;
import com.payment.gateway.dto.ThreeDSecureRequest;
import com.payment.gateway.dto.ThreeDSecureResponse;

/**
 * Bank-specific 3D Secure adapter interface
 * Her banka için farklı implementasyon sağlanır
 */
public interface BankAdapter {
    
    /**
     * Banka adı (GARANTI, ISBANK, YAPIKREDI, etc.)
     */
    String getBankName();
    
    /**
     * Bankanın desteklediği request formatı
     */
    RequestFormat getRequestFormat();
    
    /**
     * Bankanın desteklediği response formatı  
     */
    ResponseFormat getResponseFormat();
    
    /**
     * Kart numarası bu bankaya ait mi kontrol et
     */
    boolean supportsBin(String cardNumber);
    
    /**
     * 3D Secure işlemini başlat
     */
    ThreeDSecureResponse initiate3DSecure(PaymentRequest paymentRequest, ThreeDSecureRequest threeDRequest);
    
    /**
     * 3D Secure callback'ini işle
     */
    ThreeDSecureResponse handle3DCallback(String callbackData);
    
    /**
     * Bankanın test ortamı aktif mi
     */
    boolean isTestMode();
    
    /**
     * Banka konfigürasyonu geçerli mi
     */
    boolean isConfigured();
    
    enum RequestFormat {
        JSON,           // Modern REST API (JSON body)
        XML,            // XML SOAP/REST
        FORM_DATA,      // application/x-www-form-urlencoded
        MULTIPART       // multipart/form-data
    }
    
    enum ResponseFormat {
        JSON,           // JSON response
        XML,            // XML response
        HTML,           // HTML redirect page
        FORM_POST       // HTML form auto-submit
    }
}
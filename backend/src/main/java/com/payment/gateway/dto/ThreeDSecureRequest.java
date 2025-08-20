package com.payment.gateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreeDSecureRequest {
    
    private String orderId;
    private String merchantId;
    private String customerId;
    
    // Card Information
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    
    // Transaction Information
    private BigDecimal amount;
    private String currency;
    private String description;
    
    // 3D Secure Specific
    private String successUrl;      // İşlem başarılı olduğunda dönüş URL'i
    private String failUrl;         // İşlem başarısız olduğunda dönüş URL'i
    private String callbackUrl;     // Bank callback URL'i
    
    // Customer Information
    private String customerIp;
    private String customerEmail;
    private String customerPhone;
    
    // Browser Information (3D Secure v2 için)
    private String userAgent;
    private String acceptHeader;
    private String language;
    private String colorDepth;
    private String screenHeight;
    private String screenWidth;
    private String timeZone;
    private String javaEnabled;
    private String javascriptEnabled;
    
    // Additional Bank-Specific Fields
    private String installment;     // Taksit sayısı
    private String campaignCode;    // Kampanya kodu
    private String loyaltyPoints;   // Sadakat puanı kullanımı
}

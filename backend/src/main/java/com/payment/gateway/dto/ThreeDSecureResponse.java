package com.payment.gateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreeDSecureResponse {
    
    private boolean success;
    private String message;
    private ThreeDStatus status;
    
    // 3D Secure URLs
    private String redirectUrl;         // Kullanıcının yönlendirileceği 3D Secure sayfası
    private String formData;           // POST için form data (HTML)
    private String formAction;         // Form action URL
    
    // Transaction Information
    private String orderId;
    private String transactionId;
    private String authCode;
    private String hostReferenceNumber;
    
    // 3D Secure Verification
    private String threeDSecureId;
    private String cavv;               // Cardholder Authentication Verification Value
    private String eci;                // Electronic Commerce Indicator
    private String xid;                // 3D Secure Transaction ID
    
    // Bank Response
    private String bankResponseCode;
    private String bankResponseMessage;
    private String mdStatus;           // 3D Secure MD Status
    
    // Error Information
    private String errorCode;
    private String errorMessage;
    private String errorDetail;
    
    // Additional Information
    private String bankName;
    private String cardBrand;
    private String maskedCardNumber;
    
    public enum ThreeDStatus {
        INITIATED,          // 3D Secure başlatıldı, kullanıcı yönlendirilecek
        AUTHENTICATED,      // 3D Secure başarılı, ödeme yapılabilir
        FAILED,            // 3D Secure başarısız
        NOT_ENROLLED,      // Kart 3D Secure'e kayıtlı değil
        ERROR,             // Sistem hatası
        TIMEOUT,           // Timeout
        CANCELLED          // Kullanıcı iptal etti
    }
}
package com.payment.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {
    
    private String merchantId;
    private String apiKey;
    private String secretKey;
    private String message;
    
    public static ApiKeyResponse success(String merchantId, String apiKey, String secretKey) {
        return new ApiKeyResponse(merchantId, apiKey, secretKey, "API key başarıyla oluşturuldu");
    }
    
    public static ApiKeyResponse error(String merchantId, String message) {
        return new ApiKeyResponse(merchantId, null, null, message);
    }
}
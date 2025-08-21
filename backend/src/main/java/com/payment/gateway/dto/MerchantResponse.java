package com.payment.gateway.dto;

import com.payment.gateway.model.Merchant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {
    
    private Long id;
    private String merchantId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String website;
    private String status;
    private String webhookUrl;
    private String webhookEvents;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // API key ve secret key güvenlik nedeniyle response'da gönderilmez
    
    public static MerchantResponse fromEntity(Merchant merchant) {
        MerchantResponse response = new MerchantResponse();
        response.setId(merchant.getId());
        response.setMerchantId(merchant.getMerchantId());
        response.setName(merchant.getName());
        response.setEmail(merchant.getEmail());
        response.setPhone(merchant.getPhone());
        response.setAddress(merchant.getAddress());
        response.setWebsite(merchant.getWebsite());
        response.setStatus(merchant.getStatus().name());
        response.setWebhookUrl(merchant.getWebhookUrl());
        response.setWebhookEvents(merchant.getWebhookEvents());
        response.setCreatedAt(merchant.getCreatedAt());
        response.setUpdatedAt(merchant.getUpdatedAt());
        return response;
    }
}
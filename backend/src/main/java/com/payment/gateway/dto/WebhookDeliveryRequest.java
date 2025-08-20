package com.payment.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDeliveryRequest {
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    @NotBlank(message = "Event type is required")
    private String eventType;
    
    @NotNull(message = "Event data is required")
    private Object eventData;
    
    @NotBlank(message = "Entity ID is required")
    private String entityId;
    
    private String description;
}
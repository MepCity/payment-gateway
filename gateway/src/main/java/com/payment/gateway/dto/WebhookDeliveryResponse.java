package com.payment.gateway.dto;

import com.payment.gateway.model.WebhookDelivery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDeliveryResponse {
    
    private boolean success;
    private String message;
    private String deliveryId;
    private String webhookId;
    private String merchantId;
    private String eventType;
    private String targetUrl;
    private WebhookDelivery.DeliveryStatus status;
    private Integer attemptNumber;
    private Integer responseCode;
    private String responseBody;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime receivedAt;
    private Integer responseTimeMs;
    private String headers;
    private LocalDateTime createdAt;
}

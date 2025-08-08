package com.payment.gateway.dto;

import com.payment.gateway.model.Webhook;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {
    
    private boolean success;
    private String message;
    private String webhookId;
    private String merchantId;
    private String url;
    private String eventType;
    private Webhook.WebhookStatus status;
    private Integer maxRetries;
    private Integer currentRetries;
    private Integer timeoutSeconds;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime nextAttemptAt;
    private String lastError;
    private String lastResponse;
    private Integer lastResponseCode;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

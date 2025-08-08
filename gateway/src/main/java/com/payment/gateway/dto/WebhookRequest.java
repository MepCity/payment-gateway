package com.payment.gateway.dto;

import com.payment.gateway.model.Webhook;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRequest {
    
    @NotBlank(message = "Merchant ID is required")
    @Size(min = 3, max = 50, message = "Merchant ID must be between 3 and 50 characters")
    private String merchantId;
    
    @NotBlank(message = "Webhook URL is required")
    @Pattern(regexp = "^https?://.*", message = "URL must be a valid HTTP or HTTPS URL")
    @Size(max = 500, message = "URL cannot exceed 500 characters")
    private String url;
    
    @NotBlank(message = "Event type is required")
    @Pattern(regexp = "^[A-Z_]+$", message = "Event type must contain only uppercase letters and underscores")
    @Size(max = 50, message = "Event type cannot exceed 50 characters")
    private String eventType;
    
    @NotBlank(message = "Secret key is required")
    @Size(min = 16, max = 100, message = "Secret key must be between 16 and 100 characters")
    private String secretKey;
    
    @Min(value = 1, message = "Max retries must be at least 1")
    @Max(value = 10, message = "Max retries cannot exceed 10")
    private Integer maxRetries = 3;
    
    @Min(value = 5, message = "Timeout must be at least 5 seconds")
    @Max(value = 60, message = "Timeout cannot exceed 60 seconds")
    private Integer timeoutSeconds = 30;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @NotNull(message = "Active status is required")
    private Boolean isActive = true;
}

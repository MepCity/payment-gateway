package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "webhooks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String webhookId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String url;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(nullable = false)
    private String secretKey;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookStatus status;
    
    @Column(nullable = false)
    private Integer maxRetries;
    
    @Column(nullable = false)
    private Integer currentRetries;
    
    @Column(nullable = false)
    private Integer timeoutSeconds;
    
    @Column
    private LocalDateTime lastAttemptAt;
    
    @Column
    private LocalDateTime nextAttemptAt;
    
    @Column(length = 1000)
    private String lastError;
    
    @Column(length = 1000)
    private String lastResponse;
    
    @Column
    private Integer lastResponseCode;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum WebhookStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        FAILED,
        DELETED
    }
    
    public enum EventType {
        PAYMENT_CREATED,
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        PAYMENT_REFUNDED,
        PAYMENT_CANCELLED,
        MANDATE_CREATED,
        MANDATE_ACTIVATED,
        MANDATE_CANCELLED,
        MANDATE_FAILED,
        REFUND_CREATED,
        REFUND_COMPLETED,
        REFUND_FAILED,
        DISPUTE_CREATED,
        DISPUTE_UPDATED,
        DISPUTE_RESOLVED,
        PAYOUT_CREATED,
        PAYOUT_COMPLETED,
        PAYOUT_FAILED,
        CUSTOMER_CREATED,
        CUSTOMER_UPDATED,
        CUSTOMER_DELETED
    }
}

package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDelivery {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String deliveryId;
    
    @Column(nullable = false)
    private String webhookId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(nullable = false)
    private String eventData;
    
    @Column(nullable = false)
    private String targetUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;
    
    @Column(nullable = false)
    private Integer attemptNumber;
    
    @Column
    private Integer responseCode;
    
    @Column(columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column
    private LocalDateTime sentAt;
    
    @Column
    private LocalDateTime receivedAt;
    
    @Column
    private Integer responseTimeMs;
    
    @Column(columnDefinition = "TEXT")
    private String headers;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public enum DeliveryStatus {
        PENDING,
        SENT,
        DELIVERED,
        FAILED,
        TIMEOUT,
        RETRY_SCHEDULED
    }
}
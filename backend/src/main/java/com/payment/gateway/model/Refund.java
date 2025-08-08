package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Refund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String refundId;
    
    @Column(nullable = false)
    private String paymentId;
    
    @Column(nullable = false)
    private String transactionId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundReason reason;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 1000)
    private String gatewayResponse;
    
    @Column(length = 100)
    private String gatewayRefundId;
    
    @Column(nullable = false)
    private LocalDateTime refundDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum RefundStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
    
    public enum RefundReason {
        CUSTOMER_REQUEST, MERCHANT_REQUEST, DUPLICATE_PAYMENT, FRAUD, TECHNICAL_ERROR, OTHER
    }
}

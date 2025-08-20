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
@Table(name = "disputes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dispute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String disputeId;
    
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
    private DisputeStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeReason reason;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 1000)
    private String evidence;
    
    @Column(length = 1000)
    private String gatewayResponse;
    
    @Column(length = 100)
    private String gatewayDisputeId;
    
    @Column(nullable = false)
    private LocalDateTime disputeDate;
    
    @Column
    private LocalDateTime resolutionDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum DisputeStatus {
        OPENED, UNDER_REVIEW, EVIDENCE_REQUIRED, RESOLVED, CLOSED, WON, LOST, PARTIAL_REFUND
    }
    
    public enum DisputeReason {
        FRAUD, DUPLICATE, PRODUCT_NOT_RECEIVED, PRODUCT_NOT_AS_DESCRIBED, 
        CREDIT_NOT_PROCESSED, GENERAL, OTHER
    }
}

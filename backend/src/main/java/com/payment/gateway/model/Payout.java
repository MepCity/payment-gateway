package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payouts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payout {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String payoutId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutType type;
    
    @Column(nullable = false)
    private String bankAccountNumber;
    
    @Column(nullable = false)
    private String bankRoutingNumber;
    
    @Column(nullable = false)
    private String bankName;
    
    @Column(nullable = false)
    private String accountHolderName;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 1000)
    private String gatewayResponse;
    
    @Column(unique = true)
    private String gatewayPayoutId;
    
    @Column
    private LocalDateTime processedAt;
    
    @Column
    private LocalDateTime settledAt;
    
    @Column(length = 500)
    private String failureReason;
    
    @Column(length = 1000)
    private String notes;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum PayoutStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REVERSED
    }
    
    public enum PayoutType {
        BANK_TRANSFER,
        ACH_TRANSFER,
        WIRE_TRANSFER,
        SEPA_TRANSFER,
        SWIFT_TRANSFER
    }
}
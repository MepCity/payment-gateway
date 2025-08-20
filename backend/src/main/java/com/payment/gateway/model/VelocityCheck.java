package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "velocity_checks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VelocityCheck {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String checkId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VelocityType type;
    
    @Column(nullable = false)
    private String identifier; // Card number, IP, email, etc.
    
    @Column
    private String merchantId;
    
    @Column(nullable = false)
    private Integer transactionCount;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private LocalDateTime windowStart;
    
    @Column(nullable = false)
    private LocalDateTime windowEnd;
    
    @Column(nullable = false)
    private Boolean limitExceeded = false;
    
    @Column
    private Integer allowedCount;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal allowedAmount;
    
    @Column(length = 500)
    private String details;
    
    @CreationTimestamp
    @Column(name = "checked_at", nullable = false, updatable = false)
    private LocalDateTime checkedAt;
    
    public enum VelocityType {
        CARD_TRANSACTIONS_PER_MINUTE,    // Card transactions per minute
        CARD_TRANSACTIONS_PER_HOUR,      // Card transactions per hour  
        CARD_TRANSACTIONS_PER_DAY,       // Card transactions per day
        CARD_AMOUNT_PER_HOUR,            // Card amount per hour
        CARD_AMOUNT_PER_DAY,             // Card amount per day
        IP_TRANSACTIONS_PER_MINUTE,      // IP transactions per minute
        IP_TRANSACTIONS_PER_HOUR,        // IP transactions per hour
        EMAIL_TRANSACTIONS_PER_DAY,      // Email transactions per day
        MERCHANT_TRANSACTIONS_PER_MINUTE, // Merchant transactions per minute
        CUSTOMER_TRANSACTIONS_PER_HOUR   // Customer transactions per hour
    }
}
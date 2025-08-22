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
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
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
    private PaymentStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(nullable = false)
    private String cardNumber;
    
    @Column(nullable = false)
    private String cardHolderName;
    
    @Column(nullable = false)
    private String expiryDate;
    
    @Column(length = 20)
    private String cardBrand;
    
    @Column(length = 6)
    private String cardBin;
    
    @Column(length = 4)
    private String cardLastFour;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 1000)
    private String gatewayResponse;
    
    @Column(length = 100)
    private String gatewayTransactionId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public enum PaymentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
    }
    
    public enum PaymentMethod {
        CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, DIGITAL_WALLET
    }
}
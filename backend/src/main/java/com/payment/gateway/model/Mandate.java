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
@Table(name = "mandates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mandate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String mandateId;
    
    @Column(nullable = false)
    private String customerId;
    
    @Column(nullable = false)
    private String merchantId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MandateStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MandateType type;
    
    @Column(nullable = false)
    private String bankAccountNumber;
    
    @Column(nullable = false)
    private String bankSortCode;
    
    @Column(nullable = false)
    private String accountHolderName;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @Column
    private LocalDateTime endDate;
    
    @Column(nullable = false)
    private Integer frequency; // Days between payments
    
    @Column(nullable = false)
    private Integer maxPayments; // -1 for unlimited
    
    @Column(length = 1000)
    private String gatewayResponse;
    
    @Column(length = 100)
    private String gatewayMandateId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum MandateStatus {
        PENDING, ACTIVE, SUSPENDED, CANCELLED, EXPIRED, REVOKED
    }
    
    public enum MandateType {
        RECURRING, ONE_OFF, VARIABLE
    }
}
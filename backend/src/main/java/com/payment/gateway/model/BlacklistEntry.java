package com.payment.gateway.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "blacklist_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistType type;
    
    @Column(nullable = false)
    private String value; // Card number, IP, email, etc.
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistReason reason;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column
    private LocalDateTime expiresAt;
    
    @Column
    private String addedBy; // User who added this entry
    
    @Column
    private String merchantId; // If merchant-specific blacklist
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum BlacklistType {
        CARD_NUMBER,        // Full or partial card number
        CARD_BIN,          // Card BIN (first 6 digits)
        EMAIL,             // Email address
        IP_ADDRESS,        // IP address
        PHONE,             // Phone number
        DEVICE_ID,         // Device fingerprint
        USER_AGENT         // Browser user agent
    }
    
    public enum BlacklistReason {
        FRAUD_CONFIRMED,    // Confirmed fraudulent activity
        CHARGEBACK,        // High chargeback rate
        SUSPICIOUS_PATTERN, // Suspicious behavior pattern
        MANUAL_REVIEW,     // Manual decision
        COMPLIANCE,        // Regulatory compliance
        MERCHANT_REQUEST   // Merchant-specific block
    }
}
